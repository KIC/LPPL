package kic.lppl;

import com.aparapi.Range;
import kic.pool.Shutdownable;
import net.finmath.optimizer.LevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.*;
import static kic.lppl.Doubles.*;
import static kic.lppl.Floats.*;

// TODO we could theoretically constrain the parametrs m, w by returning Doble.NaN i.e. for 0.1 ≤ m ≤ 0.9 and 6 ≤ ω ≤ 13,
public class LPPLSolver extends LevenbergMarquardt implements Shutdownable {
    public static final int M = 0, W = 1, TC = 2;
    public static final double DEFAULT_PARAMETER_DIFFERENTIATOR = 1E-7;
    public static final double DEFAULT_M = 0.5;
    public static final double DEFAULT_W = 9;
    private final LPPLKernel lpplKernel;
    private final ABCCSolver abccSolver;
    private final Range range;
    private final double dFact;

    public LPPLSolver(double[] time, double[] price, double initGuessedM, double initGuessedW, Double initGuessedTC, Double diffrentationFactor) {
        this(toFloats(time), toFloats(price), price, initGuessedM, initGuessedW, initGuessedTC, diffrentationFactor);
    }

    public LPPLSolver(float[] time, float[] price, double[] priceD, double initGuessedM, double initGuessedW, Double initGuessedTC, Double diffrentationFactor) {
        this(new ABCCSolver(time, price),
             new LPPLKernel(time),
             Range.create(time.length),
             priceD,
             initGuessedM,
             initGuessedW,
             initGuessedTC != null ? initGuessedTC : time.length > 0 ? time[time.length - 1] + 1d : 0d,
             diffrentationFactor != null ? diffrentationFactor : DEFAULT_PARAMETER_DIFFERENTIATOR);
    }

    // float[] time, float[] price,
    private LPPLSolver(ABCCSolver abccSolver, LPPLKernel lpplKernel, Range range, double[] priceD, double initGuessedM, double initGuessedW, double initGuessedTC, double diffrentationFactor) {
        super(1);
        this.abccSolver = abccSolver;
        this.lpplKernel = lpplKernel;
        this.range = range;
        this.dFact = diffrentationFactor;
        this.setMaxIteration(2000);
        this.setErrorTolerance(0.01d);

        double[] weights = new double[priceD.length];
        Arrays.fill(weights, 1d);
        this.setWeights(weights);

        this.setInitialParameters(new double[]{initGuessedM, initGuessedW, initGuessedTC});
        this.setTargetValues(priceD);
    }

    public LPPLSolver withNewTarget(double[] time, double[] price, boolean useLastParamsAsInitialGuess) {
        return withNewTarget(toFloats(time), toFloats(price), price, useLastParamsAsInitialGuess);
    }

    public LPPLSolver withNewTarget(float[] time, float[] price, double[] priceD, boolean useLastParamsAsInitialGuess) {
        // since the LevenbergMarquardt solver can not bew reused once it has been run, we need to make a copy
        // while keeping all the kernels
        abccSolver.setNewTimeAndPrice(time, price);
        lpplKernel.setNewTime(time);
        boolean reuseParameters = useLastParamsAsInitialGuess && getBestFitParameters() != null;
        double newTcGuess = reuseParameters
                ? Math.max(time[time.length - 1] + 1d, getBestFitParameters()[2])
                : time[time.length - 1] + 1d;

        return new LPPLSolver(abccSolver,
                              lpplKernel,
                              Range.create(time.length),
                              priceD,
                              reuseParameters ? getBestFitParameters()[0] : DEFAULT_M,
                              reuseParameters ? getBestFitParameters()[1] : DEFAULT_W,
                              newTcGuess,
                              dFact);
    }

    @Override
    public void setValues(double[] parameters, double[] values) throws SolverException {
        double m = parameters[0];
        double w = parameters[1];
        double tc = parameters[2];
        double[] abcc = this.abccSolver.solve((float) tc, (float) m, (float) w);

        lpplKernel.setAbccMwtc(toFloats(abcc), toFloats(parameters));
        lpplKernel.execute(range);
        copyToDouble(lpplKernel.getValues(), values);

        setParameterSteps(Arrays.stream(parameters).map(p -> (Math.abs(p) + 1) * dFact).toArray());
    }

    double[] solve() {
        try {
            run();
            double[] solution = getBestFitParameters();
            double[] abcc = this.abccSolver.solve((float) solution[2], (float) solution[0], (float) solution[1]);
            return new double[]{solution[0], solution[1], round(solution[2]), abcc[0], abcc[1], abcc[2], abcc[3]};
        } catch (SolverException se) {
            throw new IllegalStateException(se);
        }
    }

    public static double[][] reconstructOszillator(double[] time, double[] mwtcabcc) {
        int lastTimeIdx = time.length - 1;
        int step = (int) max(1, floor((time[lastTimeIdx] - time[0]) / time.length));
        int extensions = (int) floor((mwtcabcc[2] - time[lastTimeIdx]) / step);
        double[][] resTimeLppl = new double[time.length + extensions][2];

        for (int i = 0; i < resTimeLppl.length; i++) {
            double t = i < time.length ? time[i] : time[lastTimeIdx] + step * (time.length - i + 1);
            double tc_t = mwtcabcc[2] - t;
            double a = pow((tc_t), mwtcabcc[0]);
            resTimeLppl[i][0] = t;
            resTimeLppl[i][1] = mwtcabcc[3] + mwtcabcc[4] * a + mwtcabcc[5] * a * cos(mwtcabcc[1] * log(tc_t)) + mwtcabcc[6] * a * sin(mwtcabcc[1] * log(tc_t));
        }

        return resTimeLppl;
    }

    @Override
    public void shutDown() {
        abccSolver.dispose();
        lpplKernel.dispose();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutDown();
    }
}

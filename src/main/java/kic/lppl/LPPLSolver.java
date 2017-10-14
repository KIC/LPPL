package kic.lppl;

import com.aparapi.Range;
import com.aparapi.device.JavaDevice;
import net.finmath.optimizer.LevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.round;
import static kic.lppl.Doubles.*;
import static kic.lppl.Floats.*;

// TODO we could theoretically constrain the parametrs m, w by returning Doble.NaN i.e. for 0.1 ≤ m ≤ 0.9 and 6 ≤ ω ≤ 13,
public class LPPLSolver extends LevenbergMarquardt {
    public static final double DEFAULT_PARAMETER_DIFFERENTIATOR = 1E-7;
    public static final double DEFAULT_M = 0.5;
    public static final double DEFAULT_W = 9;
    private final LPPLKernel lpplKernel;
    private final ABCCSolver abccSolver;
    private final Range range;
    private final double dFact;

    public LPPLSolver(float[] time, float[] price, double[] priceD, double initGuessedM, double initGuessedW, Double initGuessedTC, Double diffrentationFactor) {
        this(new ABCCSolver(time, price),
             new LPPLKernel(time),
             Range.create(time.length),
             priceD,
             initGuessedM,
             initGuessedW,
             initGuessedTC != null ? initGuessedTC : time[time.length - 1] + 1d,
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

    public LPPLSolver withNewTarget(float[] time, float[] price, double[] priceD, boolean useLastParamsAsInitialGuess) {
        // since the LevenbergMarquardt solver can not bew reused once it has been run, we need to make a copy
        // while keeping all the kernels
        abccSolver.setNewTimeAndPrice(time, price);
        lpplKernel.setNewTime(time);
        double newTcGuess = useLastParamsAsInitialGuess
                ? Math.max(time[time.length - 1] + 1d, getBestFitParameters()[2])
                : time[time.length - 1] + 1d;

        return new LPPLSolver(abccSolver,
                              lpplKernel,
                              Range.create(time.length),
                              priceD,
                              useLastParamsAsInitialGuess ? getBestFitParameters()[0] : DEFAULT_M,
                              useLastParamsAsInitialGuess ? getBestFitParameters()[1] : DEFAULT_W,
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

    double[] solve() throws SolverException {
        run();
        double[] solution = getBestFitParameters();
        solution[2] = round(solution[2]);
        return solution;
    }

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

package kic.lppl;

import net.finmath.optimizer.LevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.*;

public class LPPLSolver extends LevenbergMarquardt {
    private final ABCCSolver abcc;
    private final float[] time;

    public LPPLSolver(float[] time, float[] price, double[] priceD, double initGuessedM, double initGuessedW, double initGuessedTC) {
        super(1);
        this.time = time;
        this.abcc = new ABCCSolver(time, price);
        this.setMaxIteration(2000);
        this.setErrorTolerance(0.01d);

        double[] weights = new double[time.length];
        Arrays.fill(weights, 1d);
        this.setWeights(weights);

        this.setInitialParameters(new double[]{initGuessedM, initGuessedW, initGuessedTC});
        this.setTargetValues(priceD);
    }

    @Override
    public void setValues(double[] parameters, double[] values) throws SolverException {
        double m = parameters[0];
        double w = parameters[1];
        double tc = parameters[2];
        double[] abcc = this.abcc.solve((float) tc, (float) m, (float) w);

        for (int i = 0; i < values.length; i++) { // this for loop goes into a kernel, can we call another kernel then? at least the loop can be prallel

            double t = time[i];
            double tc_t = tc - t;
            double a = pow((tc_t), m);

            // return A + B * a + C1 * a * cos(w * log(tc - t)) + C2 * a * sin(w * log(tc - t))
            // we weould set dynamically in the kernel: a, tc_t, w
            values[i] = abcc[0] + abcc[1] * a + abcc[2] * a * cos(w * log(tc_t)) + abcc[3] * a * sin(w * log(tc_t));
        }
    }

    double[] solve() throws SolverException {
        run();
        abcc.dispose();
        double[] solution = getBestFitParameters();
        solution[2] = round(solution[2]);
        return solution;
    }

}

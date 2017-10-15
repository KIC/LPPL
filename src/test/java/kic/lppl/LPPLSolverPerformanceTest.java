package kic.lppl;

import net.finmath.optimizer.SolverException;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static kic.lppl.LPPLSolver.*;
import static kic.lppl.Doubles.*;
import static kic.DATA.*;
import static org.junit.Assert.*;

public class LPPLSolverPerformanceTest {
    private static LPPLSolver sornetteSolver;

    @BeforeClass
    public static void setUpSolver() throws SolverException {
        // do not start to close ...
        sornetteSolver = new LPPLSolver(toFloats(time), toFloats(price), price, 0.2, DEFAULT_W, null, null);
        // warm up the solver
        sornetteSolver.solve();
    }

    @Rule
    public ContiPerfRule rule = new ContiPerfRule();

    @Test
    @PerfTest(invocations = 100, threads = 1)
    public void fit() throws Exception {
        sornetteSolver = sornetteSolver.withNewTarget(toFloats(time), toFloats(price), price, false);
        double[] mwtc = sornetteSolver.solve();
        assertArrayEquals(new double[]{0.5376815158637659, 8.935918225558547, 416887.0}, mwtc, 0.0001);
    }

    @AfterClass
    public static void teadDownSolver() {
        sornetteSolver.shutDown();
    }
}
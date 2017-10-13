package kic.lppl;


import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class ABCCSolverTest {
    static final float[] time = new float[]{1000.1f, 1000.2f, 1000.3f, 1000.4f, 1000.5f, 1000.6f, 1000.7f, 1000.8f, 1000.9f, 1001f, 1001.1f, 1001.2f, 1001.3f, 1001.4f, 1001.5f, 1001.6f};
    static final float[] price = new float[]{0.722232428f, 0.341683878f, 0.078340336f, 0.608248143f, 0.604425547f, 0.596816357f, 0.517850473f, 0.34417176f, 0.993074271f, 0.527909465f, 0.787017788f, 0.797161536f, 0.919331641f, 0.562757738f, 0.904961378f, 0.825355707f};
    static ABCCSolver solver;

    @Rule
    public ContiPerfRule rule = new ContiPerfRule();

    @BeforeClass
    public static void setUpSolver() {
        solver = new ABCCSolver(time, price);
    }

    @Test
    @PerfTest(invocations = 500, threads = 1)
    public void getResult() throws Exception {
        // Assert close to: 1.2637378307194345, -0.6008272528267533, 0.021645894539625957, 9.32247644715204E-4
        final double[] expected = new double[]{1.2637378307194345, -0.6008272528267533, 0.021645894539625957, 9.32247644715204E-4};
        double[] result = solver.solve(1002f, 0.5f, 9f);
        assertArrayEquals(expected, result, 0.00001);
    }

    @AfterClass
    public static void tearDownSolver() {
        solver.dispose();
    }

}
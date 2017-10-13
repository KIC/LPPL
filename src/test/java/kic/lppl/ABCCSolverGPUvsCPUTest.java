package kic.lppl;


import com.aparapi.device.JavaDevice;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;


public class ABCCSolverGPUvsCPUTest {
    static final Random r = new Random();
    static float[] time = new float[5000];
    static float[] price = new float[5000];
    static ABCCSolver solver;

    @Rule
    public ContiPerfRule rule = new ContiPerfRule();

    @BeforeClass
    public static void setUpSolver() {
        solver = new ABCCSolver(time, price);
        for (int i=0; i<time.length; i++) {
            time[i] = 1000f + i / 10f;
            price[i] = r.nextFloat();
        }

        solver.solve(time[time.length-1] + 2f, r.nextFloat(), 9f);
        solver.solve(time[time.length-1] + 2f, r.nextFloat(), 9f, JavaDevice.THREAD_POOL);
    }

    @Test
    @PerfTest(invocations = 500, threads = 1)
    public void getGPUResult() throws Exception {
        solver.solve(time[time.length-1] + 2f, r.nextFloat(), 9f);
    }

    @Test
    @PerfTest(invocations = 500, threads = 1)
    public void getCPUResult() throws Exception {
        solver.solve(time[time.length-1] + 2f, r.nextFloat(), 9f, JavaDevice.THREAD_POOL);
    }

    @AfterClass
    public static void tearDownSolver() {
        solver.dispose();
    }

}
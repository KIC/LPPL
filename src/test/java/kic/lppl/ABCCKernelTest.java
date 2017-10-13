package kic.lppl;

import com.aparapi.Range;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ABCCKernelTest {
    static final float[] time = new float[]{1000.1f, 1000.2f, 1000.3f, 1000.4f, 1000.5f, 1000.6f, 1000.7f, 1000.8f, 1000.9f, 1001f, 1001.1f, 1001.2f, 1001.3f, 1001.4f, 1001.5f, 1001.6f};
    static final float[] price = new float[]{0.722232428f, 0.341683878f, 0.078340336f, 0.608248143f, 0.604425547f, 0.596816357f, 0.517850473f, 0.34417176f, 0.993074271f, 0.527909465f, 0.787017788f, 0.797161536f, 0.919331641f, 0.562757738f, 0.904961378f, 0.825355707f};
    static AtomicReference<Float> lastValue = new AtomicReference<>(1f);
    static final Random r = new Random();
    static ABCCKernel kernel;

    @Rule
    public ContiPerfRule rule = new ContiPerfRule();

    @BeforeClass
    public static void setUpKernel() throws Exception {
        kernel = new ABCCKernel(time, price);
    }

    @Test
    @PerfTest(invocations = 100, threads = 1)
    public void run() throws Exception {
        kernel.set_tcmw(1002f, r.nextFloat(), 9f);
        kernel.execute(Range.create(kernel.N));
        kernel.get(kernel.result);
        assertFalse(lastValue.get() == kernel.result[0]);
        lastValue.set(kernel.result[0]);
    }

    @AfterClass
    public static void release() {
        kernel.dispose();
    }
}
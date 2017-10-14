package kic.lppl;

import com.aparapi.Kernel;

public class LPPLKernel extends Kernel {
    private float[] values;
    private float[] abcc, mwtc;
    private float[] time;

    public LPPLKernel(float[] time) {
        setExplicit(true);
        this.time = time;
        this.values = new float[time.length];
        put(this.time).put(this.values);
    }

    public void setNewTime(float[] time) {
        this.time = time;
        this.values = new float[time.length];
        put(this.time).put(this.values);
    }

    public void setAbccMwtc(float[] abcc, float[] mwtc) {
        this.abcc = abcc;
        this.mwtc = mwtc;
        put(this.abcc).put(mwtc);
    }

    @Override
    public void run() {
        int i = getGlobalId();
        float t = time[i];
        float w = mwtc[1];
        float tc_t = mwtc[2] - t;
        float a = pow((tc_t), mwtc[0]);

        // return A + B * a + C1 * a * cos(w * log(tc - t)) + C2 * a * sin(w * log(tc - t))
        values[i] =  abcc[0] + abcc[1] * a + abcc[2] * a * cos(w * log(tc_t)) + abcc[3] * a * sin(w * log(tc_t));
    }

    public float[] getValues() {
        get(this.values);
        return values;
    }
}

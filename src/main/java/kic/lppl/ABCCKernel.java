package kic.lppl;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

public class ABCCKernel extends Kernel {
    public static final int FI = 0, GI = 1, HI = 2, YI = 3, FI2 = 4, FIGI = 5, GI2 = 6, YIFI = 7, YIGI = 8, FIHI = 9, GIHI = 10, HI2 = 11, YIHI = 12;
    public static final int v = 13;
    private static final int TC = 0, M = 1, W = 2;
    private final float[] T, p;
    private float[] tcmw = new float[3];
    final float[] result;
    public final int N;

    public ABCCKernel(float[] t, float[] p) {
        setExplicit(true);
        this.T = t;
        this.p = p;
        this.N = p.length;
        this.result = new float[N * v];
        put(this.T).put(this.p);
    }

    public void set_tcmw(float tc, float m, float w) {
        this.tcmw = new float[]{tc, m, w};
        put(this.tcmw);
    }

    @Override
    public void run() {
        int i = getGlobalId();
        int j = i * v;
        int fi = FI + j, gi = GI + j, hi = HI + j, yi = YI + j, fi2 = FI2 + j, figi = FIGI + j, gi2 = GI2 + j, yifi = YIFI + j, yigi = YIGI + j, fihi = FIHI + j, gihi = GIHI + j, hi2 = HI2 + j, yihi = YIHI + j;
        float tc = tcmw[TC];
        float w = tcmw[W];
        float m = tcmw[M];

        result[fi] = pow((tc - T[i]), m);
        result[gi] = result[fi] * cos(w * log(tc - T[i]));
        result[hi] = result[fi] * sin(w * log(tc - T[i]));
        result[yi] = p[i];
        result[fi2] = result[fi] * result[fi];
        result[figi] = result[fi] * result[gi];
        result[gi2] = result[gi] * result[gi];
        result[yifi] = result[yi] * result[fi];
        result[yigi] = result[yi] * result[gi];
        result[fihi] = result[fi] * result[hi];
        result[gihi] = result[gi] * result[hi];
        result[hi2] = result[hi] * result[hi];
        result[yihi] = result[yi] * result[hi];
    }

}

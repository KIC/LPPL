package kic.lppl;

public class Floats {
    public static final double[] toDouble(final float[] floats) {
        if (floats == null) return null;
        double[] res = new double[floats.length];
        for (int i=0; i<res.length; i++) res[i] = floats[i];
        return res;
    }

    public static final double[] copyToDouble(final float[] floats, final double[] res) {
        if (floats == null) return res;
        for (int i=0; i<res.length; i++) res[i] = floats[i];
        return res;
    }
}

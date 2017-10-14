package kic.lppl;

public class Doubles {
    public static float[] toFloats(final double[] doubles) {
        if (doubles == null) return null;
        float[] res = new float[doubles.length];
        for (int i=0; i<res.length; i++) res[i] = (float) doubles[i];
        return res;
    }
}

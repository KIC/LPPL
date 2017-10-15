package kic.lppl;

public class Doubles {
    public static float[] toFloats(final double[] doubles) {
        if (doubles == null) return null;
        float[] res = new float[doubles.length];
        for (int i=0; i<res.length; i++) res[i] = (float) doubles[i];
        return res;
    }

    public static float[] toFloats(final double[] doubles, int fromIndex) {
        if (doubles == null) return null;
        if (fromIndex < 0) fromIndex = doubles.length + fromIndex;

        float[] res = new float[doubles.length - fromIndex];
        for (int i=0; i<res.length; i++) res[i] = (float) doubles[i + fromIndex];
        return res;
    }

    public static double[] fromIndex(double[] doubles, int fromIndex) {
        if (doubles == null) return null;
        if (fromIndex < 0) fromIndex = doubles.length + fromIndex;
        double[] res = new double[doubles.length - fromIndex];
        System.arraycopy(doubles, fromIndex, res, 0, res.length);
        return res;
    }

}

package kic.lppl;

import com.aparapi.Range;
import com.aparapi.device.Device;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;

import static kic.lppl.ABCCKernel.*;

public class ABCCSolver {
    private final ABCCKernel kernel;

    public ABCCSolver(float[] time, float[] price) {
        this.kernel = new ABCCKernel(time, price);
    }

    public double[] solve(float tc, float m, float w) {
        return solve(tc, m, w, Range.create(kernel.N));
    }

    public double[] solve(float tc, float m, float w, Device device) {
        return solve(tc, m, w, device.createRange(kernel.N));
    }

    private double[] solve(float tc, float m, float w, Range range) {
        kernel.set_tcmw(tc, m, w);
        kernel.execute(range);
        kernel.get(kernel.result);
        final float[] result = kernel.result;
        double[][] _A = new double[4][4];
        double[] _b = new double[4];
        _A[0][0] = kernel.N;

        /*
          A = [[ N,           result[fi],    result[gi],    result[hi]   ],
               [ result[fi],  result[fi2],   result[figi],  result[fihi] ],
               [ result[gi],  result[figi],  result[gi2],   result[gihi] ],
               [ result[hi],  result[fihi],  result[gihi],  result[hi2]  ]]
          b =  [ sum_yi,      sum_yi_fi,     sum_yi_gi,     sum_yi_hi ]
       */
        for (int i = 0; i < kernel.N; i++) {
            int offset = i * v;
            _A[0][1] += result[offset + FI];
            _A[0][2] += result[offset + GI];
            _A[0][3] += result[offset + HI];

            _A[1][0] += result[offset + FI];
            _A[1][1] += result[offset + FI2];
            _A[1][2] += result[offset + FIGI];
            _A[1][3] += result[offset + FIHI];

            _A[2][0] += result[offset + GI];
            _A[2][1] += result[offset + FIGI];
            _A[2][2] += result[offset + GI2];
            _A[2][3] += result[offset + GIHI];

            _A[3][0] += result[offset + HI];
            _A[3][1] += result[offset + FIHI];
            _A[3][2] += result[offset + GIHI];
            _A[3][3] += result[offset + HI2];

            _b[0] += result[offset + YI];
            _b[1] += result[offset + YIFI];
            _b[2] += result[offset + YIGI];
            _b[3] += result[offset + YIHI];
        }

        RealMatrix A = new Array2DRowRealMatrix(_A, false);
        RealMatrix b = new Array2DRowRealMatrix(new double[][]{_b}, false).transpose();
        RealMatrix At = A.transpose();
        RealMatrix x = MatrixUtils.inverse(At.multiply(A)).multiply(At).multiply(b);

        return x.getColumn(0);
    }

    public void dispose() {
        kernel.dispose();
    }
}

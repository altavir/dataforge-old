package hep.dataforge.maths.groovy.linear

import groovy.transform.CompileStatic
import org.apache.commons.math3.linear.*

/**
 * Created by darksnake on 01-Jul-16.
 */
@CompileStatic
class LinearUtils {
    /**
     * Build identity matrix with given dimension multiplied by given value
     * @param dim
     * @param val
     * @return
     */
    public static RealMatrix identity(int dim, double val){
        List diag = new ArrayList();
        for(int i = 0; i< dim; i++){
            diag.add(val);
        }
        return new DiagonalMatrix(diag as double[]);
    }

    public static RealMatrix matrix(double[][] values){
        return new Array2DRowRealMatrix(values);
    }

    public static RealMatrix matrix(List<List<? extends Number>> values){
        double [][] dvals = values as double[][];
        return new Array2DRowRealMatrix(dvals);
    }

    public static RealVector vector(double[] values){
        return new ArrayRealVector(values);
    }

    public static RealVector vector(Collection<Double> values){
        return new ArrayRealVector(values as double[]);
    }

}

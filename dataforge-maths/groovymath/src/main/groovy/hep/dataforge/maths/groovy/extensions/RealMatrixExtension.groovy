package hep.dataforge.maths.groovy.extensions

import hep.dataforge.maths.groovy.linear.LinearUtils
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

/**
 * Created by darksnake on 01-Jul-16.
 */
class RealMatrixExtension {

    //TODO add number to matrix conversion

    /**
     * Return new map and apply given transformation to each of its elements. Closure takes 3 arguments: row number,
     * column number and actual value of matrix cell.
     * @param self
     * @param func
     * @return
     */
    static RealMatrix map(final RealMatrix self, Closure<Double> func) {
        RealMatrix res = self.copy();
        res.walkInColumnOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            double visit(int row, int column, double value) {
                func.call(row, column, value);
            }
        })
    }

    static RealMatrix plus(final RealMatrix self, RealMatrix other) {
        return self.add(other)
    }

    /**
     * Add identity matrix x num to this matrix
     * @param self
     * @param num
     * @return
     */
    static RealMatrix plus(final RealMatrix self, Number num) {
        return self.add(LinearUtils.identity(self.rowDimension, num))
    }

    static RealMatrix minus(final RealMatrix self, Number num) {
        return self.subtract(LinearUtils.identity(self.rowDimension, num))
    }

    static RealMatrix minus(final RealMatrix self, RealMatrix other) {
        return self.subtract(other)
    }

    static RealMatrix negative(final RealMatrix self) {
        return self.map { row, col, val -> -val }
    }

    static RealMatrix multiply(final RealMatrix self, Number num) {
        return self.scalarMultiply(num)
    }

    static RealMatrix multiply(final RealMatrix self, RealVector vector) {
        return self.operate(vector);
    }

    static RealMatrix div(final RealMatrix self, Number num) {
        return self.scalarMultiply(1d/num)
    }

    //TODO add getAt and setAt
}

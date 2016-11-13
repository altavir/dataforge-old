package hep.dataforge.maths.groovy.extensions

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

/**
 * Created by darksnake on 06-Nov-16.
 */
class NumberExtension {
    static RealVector plus(final Number self, RealVector other) {
        return other + self
    }

    static RealVector minus(final Number self, RealVector other) {
        return (-other) + self
    }

    static RealVector multiply(final Number self, RealVector other) {
        return other * self;
    }

    static RealVector plus(final Number self, RealMatrix other) {
        return other + self
    }

    static RealVector minus(final Number self, RealMatrix other) {
        return (-other) + self
    }

    static RealVector multiply(final Number self, RealMatrix other) {
        return other * self;
    }

    static RealVector plus(final Number self, UnivariateFunction other) {
        return other + self
    }

    static RealVector minus(final Number self, UnivariateFunction other) {
        return (-other) + self
    }

    static RealVector multiply(final Number self, UnivariateFunction other) {
        return other * self;
    }

    static RealVector plus(final Number self, UnivariateDifferentiableFunction other) {
        return other + self
    }

    static RealVector minus(final Number self, UnivariateDifferentiableFunction other) {
        return (-other) + self
    }

    static RealVector multiply(final Number self, UnivariateDifferentiableFunction other) {
        return other * self;
    }
}

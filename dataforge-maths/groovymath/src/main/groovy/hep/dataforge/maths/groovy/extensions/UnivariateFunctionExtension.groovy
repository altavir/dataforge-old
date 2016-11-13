package hep.dataforge.maths.groovy.extensions

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.linear.RealVector

/**
 * A static extension for commons-math UnivariateFunctions
 * Created by darksnake on 06-Nov-15.
 */
class UnivariateFunctionExtension {

    static Number value(final UnivariateFunction self, Number x){
        return self.value(x.doubleValue())
    }

    static Number call(final UnivariateFunction self, Number x) {
        return value(self, x)
    }

    static UnivariateFunction plus(final UnivariateFunction self, UnivariateFunction function) {
        return { x -> self.value(x) + function.value(x) }
    }

    static UnivariateFunction plus(final UnivariateFunction self, Number num) {
        return { x -> self.value(x) + num }
    }

    static UnivariateFunction minus(final UnivariateFunction self, UnivariateFunction function) {
        return { x -> self.value(x) - function.value(x) }
    }

    static UnivariateFunction minus(final UnivariateFunction self, Number num) {
        return { x -> self.value(x) - num }
    }

    static UnivariateFunction multiply(final UnivariateFunction self, UnivariateFunction function) {
        return { x -> self.value(x) * function.value(x) }
    }

    static UnivariateFunction multiply(final UnivariateFunction self, Number num) {
        return { x -> self.value(x) * num }
    }

    static UnivariateFunction div(final UnivariateFunction self, UnivariateFunction function) {
        return { x -> self.value(x) / function.value(x) }
    }

    static UnivariateFunction div(final UnivariateFunction self, Number num) {
        return { x -> self.value(x) / num }
    }

    static UnivariateFunction power(final UnivariateFunction self, UnivariateFunction function) {
        return { x -> self.value(x)**(function.value(x)) }
    }

    static UnivariateFunction power(final UnivariateFunction self, Number num) {
        return { x -> self.value(x)**(num) }
    }

    static UnivariateFunction negative(final UnivariateFunction self) {
        return { x -> -self.value(x) }
    }

    static RealVector value(final UnivariateFunction self, RealVector vector){
        return
    }


}


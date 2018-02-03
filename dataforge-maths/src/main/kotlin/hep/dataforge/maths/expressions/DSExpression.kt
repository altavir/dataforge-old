package hep.dataforge.maths.expressions

import hep.dataforge.names.Names
import org.apache.commons.math3.Field
import org.apache.commons.math3.RealFieldElement
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure

class DSExpression(val ds: DerivativeStructure, val names: Names) : RealFieldElement<DSExpression>, Number() {
    val order = ds.order

    //TODO parameter names

    override fun toByte(): Byte = ds.value.toByte()

    override fun toChar(): Char = ds.value.toChar()

    override fun toDouble(): Double = ds.value

    override fun toFloat(): Float = ds.value.toFloat()

    override fun toInt(): Int = ds.value.toInt()

    override fun toLong(): Long = ds.value.toLong()

    override fun toShort(): Short = ds.value.toShort()

    /**
     * Temporary utility override
     * TODO move to extensions
     */
    private operator fun Names.plus(other: Names): Names {
        return this.plus(*other.asArray())
    }

    operator fun plus(num: Number): Number {
        return if (num is DSExpression) {
            DSExpression(ds.add(num.ds), names + num.names)
        } else {
            DSExpression(ds.add(num.toDouble()), names)
        }
    }

    operator fun minus(num: Number): Number {
        return if (num is DSExpression) {
            DSExpression(ds.subtract(num.ds), names + num.names)
        } else {
            DSExpression(ds.subtract(num.toDouble()), names)
        }
    }

    operator fun div(num: Number): Number {
        return if (num is DSExpression) {
            DSExpression(ds.divide(num.ds),names + num.names)
        } else {
            DSExpression(ds.divide(num.toDouble()), names)
        }
    }

    operator fun times(num: Number): Number {
        return if (num is DSExpression) {
            DSExpression(ds.multiply(num.ds),names + num.names)
        } else {
            DSExpression(ds.multiply(num.toDouble()), names)
        }
    }

    operator fun rem(num: Number): Number {
        return if (num is DSExpression) {
            DSExpression(ds.remainder(num.ds), names + num.names)
        } else {
            DSExpression(ds.remainder(num.toDouble()), names)
        }
    }

    operator fun unaryMinus(): Number {
        return DSExpression(ds.negate(), names)
    }

    override fun multiply(a: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun multiply(n: Int): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun multiply(a: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun floor(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remainder(a: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remainder(a: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asin(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun scalb(n: Int): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hypot(y: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tan(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rootN(n: Int): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a: Array<out DSExpression>?, b: Array<out DSExpression>?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a: DoubleArray?, b: Array<out DSExpression>?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: DSExpression?, b1: DSExpression?, a2: DSExpression?, b2: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: DSExpression?, a2: Double, b2: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: DSExpression?, b1: DSExpression?, a2: DSExpression?, b2: DSExpression?, a3: DSExpression?, b3: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: DSExpression?, a2: Double, b2: DSExpression?, a3: Double, b3: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: DSExpression?, b1: DSExpression?, a2: DSExpression?, b2: DSExpression?, a3: DSExpression?, b3: DSExpression?, a4: DSExpression?, b4: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: DSExpression?, a2: Double, b2: DSExpression?, a3: Double, b3: DSExpression?, a4: Double, b4: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun log(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(a: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(a: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rint(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reciprocal(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subtract(a: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subtract(a: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pow(p: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pow(n: Int): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pow(e: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sin(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun negate(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cbrt(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getField(): Field<DSExpression> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun abs(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asinh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun round(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sqrt(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tanh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun log1p(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun divide(a: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun divide(a: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exp(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun atan(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun atanh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signum(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun acosh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sinh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun expm1(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cos(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun atan2(x: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cosh(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ceil(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copySign(sign: DSExpression?): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copySign(sign: Double): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReal(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun acos(): DSExpression {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}


package hep.dataforge.maths.expressions

import hep.dataforge.names.Names
import org.apache.commons.math3.Field
import org.apache.commons.math3.FieldElement
import org.apache.commons.math3.RealFieldElement
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure


/**
 * A field class for automatically differentiated numbers
 */
@Deprecated("For test only")
data class ADField(val order: Int, val names: Names) : Field<AD> {

    constructor(order: Int, vararg names: String) : this(order, Names.of(*names))

    override fun getOne(): AD {
        return AD(DerivativeStructure(names.size(), order, 0.0), this);
    }

    override fun getZero(): AD {
        return AD(DerivativeStructure(names.size(), order, 1.0), this);
    }

    override fun getRuntimeClass(): Class<out FieldElement<AD>> = AD::class.java


    fun variable(varName: String, value: Number): AD {
        if (!names.contains(varName)) {
            //TODO add conversions probably
            throw RuntimeException("Name $varName is not a part of the number context")
        }
        return AD(DerivativeStructure(names.size(), order, names.getNumberByName(varName), value.toDouble()), this)
    }

    fun const(value: Number): AD {
        return AD(DerivativeStructure(names.size(), order, value.toDouble()), this)
    }
}

/**
 * Automatically differentiated numbers
 */
@Deprecated("For test only")
class AD(val ds: DerivativeStructure, private val field: ADField) : RealFieldElement<AD>, Number() {

    override fun toByte(): Byte = ds.value.toByte()

    override fun toChar(): Char = ds.value.toChar()

    override fun toDouble(): Double = ds.value

    override fun toFloat(): Float = ds.value.toFloat()

    override fun toInt(): Int = ds.value.toInt()

    override fun toLong(): Long = ds.value.toLong()

    override fun toShort(): Short = ds.value.toShort()

    override fun getField(): Field<AD> {
        return field
    }

    /**
     * Wrap a news structure using this as a reference
     */
    private fun wrap(newValue: DerivativeStructure): AD {
        return AD(newValue, this.field)
    }

    private fun wrapWithOther(newValue: DerivativeStructure, other: AD): AD {
        if (this.field != other.field) {
            //TODO implement field transformation
            throw RuntimeException("Can't operate on two numbers defined in different contexts")
        }
        return wrap(newValue)
    }


    fun deriv(parName: String): Double {
        return deriv(mapOf(parName to 1))
    }

    fun deriv(orders: Map<String, Int>): Double {
        return ds.getPartialDerivative(*field.names.map { orders[it] ?: 0 }.toIntArray())
    }

    operator fun plus(arg: AD): AD = this.add(arg)

    operator fun minus(arg: AD): AD = this.subtract(arg)

    operator fun div(arg: AD): AD = this.divide(arg)

    operator fun times(arg: AD): AD = this.multiply(arg)

    operator fun plus(arg: Number): AD = this.add(arg.toDouble())

    operator fun minus(arg: Number): AD = this.subtract(arg.toDouble())

    operator fun div(arg: Number): AD = this.divide(arg.toDouble())

    operator fun times(arg: Number): AD = this.multiply(arg.toDouble())

    operator fun unaryMinus(): AD = this.negate()

    override fun multiply(a: Double): AD {
        return wrap(ds.multiply(a))
    }

    override fun multiply(n: Int): AD {
        return wrap(ds.multiply(n))
    }

    override fun multiply(a: AD): AD {
        return wrapWithOther(ds.multiply(a.ds), a)
    }

    override fun floor(): AD {
        return wrap(this.ds.floor())
    }

    override fun remainder(a: Double): AD {
        return wrap(ds.remainder(a))
    }

    override fun remainder(a: AD): AD {
        return wrapWithOther(ds.remainder(a.ds), a)
    }

    override fun asin(): AD {
        return wrap(this.ds.asin())
    }

    override fun scalb(n: Int): AD {
        return wrap(this.ds.scalb(n))
    }

    override fun hypot(y: AD): AD {
        return wrapWithOther(this.ds.hypot(y.ds), y)
    }

    override fun tan(): AD {
        return wrap(this.ds.tan())
    }

    override fun rootN(n: Int): AD {
        return wrap(this.ds.rootN(n))
    }

    override fun linearCombination(a: Array<out AD>, b: Array<out AD>): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a: DoubleArray, b: Array<out AD>): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: AD?, b1: AD, a2: AD, b2: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: AD, a2: Double, b2: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: AD, b1: AD, a2: AD, b2: AD, a3: AD, b3: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: AD, a2: Double, b2: AD, a3: Double, b3: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: AD, b1: AD, a2: AD, b2: AD, a3: AD, b3: AD, a4: AD, b4: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linearCombination(a1: Double, b1: AD, a2: Double, b2: AD, a3: Double, b3: AD, a4: Double, b4: AD): AD {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun log(): AD {
        return wrap(this.ds.log())
    }

    override fun add(a: Double): AD {
        return wrap(ds.add(a))
    }

    override fun add(a: AD): AD {
        return wrapWithOther(ds.add(a.ds), a)
    }

    override fun rint(): AD {
        return wrap(this.ds.rint())
    }

    override fun reciprocal(): AD {
        return wrap(this.ds.reciprocal())
    }

    override fun subtract(a: Double): AD {
        return wrap(ds.subtract(a))
    }

    override fun subtract(a: AD): AD {
        return wrapWithOther(ds.subtract(a.ds), a)
    }

    override fun pow(p: Double): AD {
        return wrap(this.ds.pow(p))
    }

    override fun pow(n: Int): AD {
        return wrap(this.ds.pow(n))
    }

    override fun pow(e: AD): AD {
        return wrapWithOther(this.ds.pow(e.ds), e)
    }

    override fun sin(): AD {
        return wrap(this.ds.sin())
    }

    override fun negate(): AD {
        return wrap(this.ds.negate())
    }

    override fun cbrt(): AD {
        return wrap(this.ds.cbrt())
    }

    override fun abs(): AD {
        return wrap(this.ds.abs())
    }

    override fun asinh(): AD {
        return wrap(this.ds.asinh())
    }

    override fun round(): Long {
        return this.toLong()
    }

    override fun sqrt(): AD {
        return wrap(this.ds.sqrt())
    }

    override fun tanh(): AD {
        return wrap(this.ds.tanh())
    }

    override fun log1p(): AD {
        return wrap(this.ds.log1p())
    }

    override fun divide(a: Double): AD {
        return wrap(ds.divide(a))
    }

    override fun divide(a: AD): AD {
        return wrapWithOther(ds.divide(a.ds), a)
    }

    override fun exp(): AD {
        return wrap(this.ds.exp())
    }

    override fun atan(): AD {
        return wrap(this.ds.atan())
    }

    override fun atanh(): AD {
        return wrap(this.ds.atanh())
    }

    override fun signum(): AD {
        return wrap(this.ds.signum())
    }

    override fun acosh(): AD {
        return wrap(this.ds.acosh())
    }

    override fun sinh(): AD {
        return wrap(this.ds.sinh())
    }

    override fun expm1(): AD {
        return wrap(this.ds.expm1())
    }

    override fun cos(): AD {
        return wrap(this.ds.cos())
    }

    override fun atan2(x: AD): AD {
        return wrapWithOther(this.ds.atan2(x.ds), x)
    }

    override fun cosh(): AD {
        return wrap(this.ds.cosh())
    }

    override fun ceil(): AD {
        return wrap(this.ds.ceil())
    }

    override fun copySign(sign: AD): AD {
        return wrapWithOther(this.ds.copySign(sign.ds), sign)
    }

    override fun copySign(sign: Double): AD {
        return wrap(this.ds.copySign(sign))
    }

    override fun getReal(): Double {
        return this.toDouble()
    }

    override fun acos(): AD {
        return wrap(this.ds.acos())
    }

}

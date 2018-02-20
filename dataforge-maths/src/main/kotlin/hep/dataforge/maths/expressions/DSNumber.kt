package hep.dataforge.maths.expressions

import hep.dataforge.names.Names
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure

class DSNumber(val ds: DerivativeStructure, val names: Names) : Number() {

    fun deriv(parName: String): Double {
        return deriv(mapOf(parName to 1))
    }

    fun deriv(orders: Map<String, Int>): Double {
        return ds.getPartialDerivative(*names.map { orders[it] ?: 0 }.toIntArray())
    }

    override fun toByte(): Byte = ds.value.toByte()

    override fun toChar(): Char = ds.value.toChar()

    override fun toDouble(): Double = ds.value

    override fun toFloat(): Float = ds.value.toFloat()

    override fun toInt(): Int = ds.value.toInt()

    override fun toLong(): Long = ds.value.toLong()

    override fun toShort(): Short = ds.value.toShort()

    /**
     * Return new DSNumber, obtained by applying given function to underlying ds
     */
    inline fun eval(func: (DerivativeStructure) -> DerivativeStructure): DSNumber {
        return DSNumber(func(ds), names)
    }
}

class DSNumberContext(val order: Int, val names: Names) : ExpressionContext<DSNumber>, ExtendedNumberContext<DSNumber> {

    constructor(order: Int, vararg names: String) : this(order, Names.of(*names))

    override fun transform(n: Number): DSNumber {
        return if (n is DSNumber) {
            if (n.names == this.names) {
                n
            } else {
                //TODO add conversion
                throw RuntimeException("Names mismatch in derivative structure")
            }
        } else {
            DSNumber(DerivativeStructure(names.size(), order, n.toDouble()), names)
        }
    }

    override fun variable(name: String, value: Number): DSNumber {
        if (!names.contains(name)) {
            //TODO add conversions probably
            throw RuntimeException("Name $name is not a part of the number context")
        }
        return DSNumber(DerivativeStructure(names.size(), order, names.getNumberByName(name), value.toDouble()), names)
    }

    override fun Number.plus(b: Number): DSNumber {
        return if (b is DSNumber) {
            transform(this).eval { it.add(b.ds) }
        } else {
            transform(this).eval { it.add(b.toDouble()) }
        }
    }

    override fun Number.minus(b: Number): DSNumber {
        return if (b is DSNumber) {
            transform(this).eval { it.subtract(b.ds) }
        } else {
            transform(this).eval { it.subtract(b.toDouble()) }
        }

    }

    override fun Number.div(b: Number): DSNumber {
        return if (b is DSNumber) {
            transform(this).eval { it.divide(b.ds) }
        } else {
            transform(this).eval { it.divide(b.toDouble()) }
        }

    }

    override fun Number.times(b: Number): DSNumber {
        return when (b) {
            is DSNumber -> transform(this).eval { it.multiply(b.ds) }
            is Int -> transform(this).eval { it.multiply(b) }
            else -> transform(this).eval { it.multiply(b.toDouble()) }
        }

    }

    override fun Number.unaryMinus(): DSNumber {
        return (this as? DSNumber)?.eval { it.negate() } ?: transform(-this.toDouble())
    }

    override fun sin(n: Number): DSNumber {
        return transform(n).eval { it.sin() }
    }

    override fun cos(n: Number): DSNumber {
        return transform(n).eval { it.cos() }
    }

    override fun exp(n: Number): DSNumber {
        return transform(n).eval { it.exp() }
    }

    override fun pow(n: Number, p: Number): DSNumber {
        return when (p) {
            is Int -> transform(n).eval { it.pow(p) }
            is DSNumber -> transform(n).eval { it.pow(p.ds) }
            else -> transform(n).eval { it.pow(p.toDouble()) }
        }
    }
}
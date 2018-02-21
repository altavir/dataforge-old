/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hep.dataforge.maths.expressions

/**
 * A context for mathematical operations on specific type of numbers. The input could be defined as any number,
 * but the output is always typed as given type.
 */
interface NumberContext<out N : Number> {
    /**
     * Arithmetical sum of arguments. The
     */
    operator fun Number.plus(b: Number): N // looks like  N plus(a:Number, b: Number) in java

    operator fun Number.minus(b: Number): N
    operator fun Number.div(b: Number): N
    operator fun Number.times(b: Number): N

    operator fun Number.unaryMinus(): N

    /**
     * Transform arbitrary number into chosen number representation.
     * Throws an exception if transformation is not available.
     */
    fun transform(n: Number): N

}

/**
 * Additional operations that could be performed on numbers in context
 */
interface ExtendedNumberContext<out N : Number> : NumberContext<N> {
    fun sin(n: Number): N
    fun cos(n: Number): N
    fun exp(n: Number): N
    fun pow(n: Number, p: Number): N

//    fun reminder(a: Number, b: Number): N
    //TODO etc
}

/**
 * Additional tools to work with expressions
 */
interface ExpressionContext<out N : Number> : NumberContext<N> {
    fun variable(name: String, value: Number): N
}


/**
 * Backward compatibility class for connoms-math/commons-numbers field/
 */
abstract class FieldCompat<out N : Number, out C : NumberContext<N>>(val nc: C) : Number() {
    abstract val self: N
    operator fun plus(n: Number): N {
        return with(nc) { self.plus(n) }
    }

    operator fun minus(n: Number): N {
        return with(nc) { self.minus(n) }
    }

    operator fun times(n: Number): N {
        return with(nc) { self.times(n) }
    }

    operator fun div(n: Number): N {
        return with(nc) { self.div(n) }
    }

    operator fun unaryMinus(): N {
        return with(nc) { self.unaryMinus() }
    }

    //A temporary fix for https://youtrack.jetbrains.com/issue/KT-22972
    abstract override fun toByte(): Byte

    abstract override fun toChar(): Char

    abstract override fun toDouble(): Double

    abstract override fun toFloat(): Float

    abstract override fun toInt(): Int

    abstract override fun toLong(): Long

    abstract override fun toShort(): Short

}

abstract class AbstractNumberContext<N : Number> : NumberContext<N> {

    protected abstract fun plusInternal(a: N, b: N): N

    override operator fun Number.plus(b: Number): N {
        return plusInternal(transform(this), transform(b))
    }

    protected abstract fun minusInternal(a: N, b: N): N

    override operator fun Number.minus(b: Number): N {
        return minusInternal(transform(this), transform(b))
    }

    protected abstract fun divInternal(a: N, b: N): N

    override operator fun Number.div(b: Number): N {
        return divInternal(transform(this), transform(b))
    }

    protected abstract fun timesInternal(a: N, b: N): N

    override operator fun Number.times(b: Number): N {
        return timesInternal(transform(this), transform(b))
    }

    protected abstract fun unaryMinusInternal(n: N): N

    override operator fun Number.unaryMinus(): N {
        return unaryMinusInternal(transform(this))
    }
}
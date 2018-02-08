package hep.dataforge.maths.extensions

import org.apache.commons.math3.RealFieldElement

/**
 * Apply unary operator to real field element in a type safe way
 */
@Suppress("UNCHECKED_CAST")
private inline fun <T> RealFieldElement<T>.unary(trans: RealFieldElement<T>.() -> T): RealFieldElement<T> {
    return trans(this) as RealFieldElement<T>
}

/**
 * apply binary operation to real field element in a type safe way
 */
@Suppress("UNCHECKED_CAST")
private inline fun <T> RealFieldElement<T>.binary(
        arg: T,
        trans: RealFieldElement<T>.(T) -> T): RealFieldElement<T> {
    return trans(this, arg) as RealFieldElement<T>
}

operator fun <T> RealFieldElement<T>.plus(arg: T): RealFieldElement<T> = binary(arg) { add(arg) }

operator fun <T> RealFieldElement<T>.minus(arg: T): RealFieldElement<T> = binary(arg) { subtract(arg) }

operator fun <T> RealFieldElement<T>.div(arg: T): RealFieldElement<T> = binary(arg) { divide(arg) }

operator fun <T> RealFieldElement<T>.times(arg: T): RealFieldElement<T> = binary(arg) { multiply(arg) }

fun <T> abs(arg: RealFieldElement<T>): RealFieldElement<T> = arg.unary { abs() }

fun <T> ceil(arg: RealFieldElement<T>): RealFieldElement<T> = arg.unary { ceil() }

fun <T> floor(arg: RealFieldElement<T>): RealFieldElement<T> = arg.unary { floor() }

fun <T> rint(arg: RealFieldElement<T>): RealFieldElement<T> = arg.unary { rint() }

fun <T> sin(arg: RealFieldElement<T>): RealFieldElement<T> = arg.unary { sin() }

//TODO add everything else
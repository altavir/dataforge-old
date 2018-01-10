package hep.dataforge.kodex

import java.math.BigDecimal

/**
 * Extension of basic java classes
 * Created by darksnake on 29-Apr-17.
 */

//Number

/**
 * Convert a number to BigDecimal
 */
fun Number.toBigDecimal(): BigDecimal {
    if (this is BigDecimal) {
        return this
    } else {
        return BigDecimal(this.toDouble())
    }
}

operator fun Number.plus(other: Number): Number {
    return this.toBigDecimal().add(other.toBigDecimal());
}

operator fun Number.minus(other: Number): Number {
    return this.toBigDecimal().subtract(other.toBigDecimal());
}

operator fun Number.div(other: Number): Number {
    return this.toBigDecimal().divide(other.toBigDecimal());
}

operator fun Number.times(other: Number): Number {
    return this.toBigDecimal().multiply(other.toBigDecimal());
}

operator fun Number.compareTo(other: Number): Int {
    return this.toBigDecimal().compareTo(other.toBigDecimal());
}
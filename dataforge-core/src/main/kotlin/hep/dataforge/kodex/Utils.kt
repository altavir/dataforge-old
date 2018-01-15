package hep.dataforge.kodex

import java.util.*

val <T> Optional<T>?.nullable: T?
    get() = this?.orElse(null)
package hep.dataforge

import java.util.stream.Collectors
import java.util.stream.Stream

fun <T> Stream<T>.toList(): List<T> {
    return collect(Collectors.toList())
}
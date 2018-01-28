package hep.dataforge.kodex

import java.io.OutputStream
import java.util.*

val <T> Optional<T>?.nullable: T?
    get() = this?.orElse(null)


object IO {
    /**
     * Create an output stream that copies its output into each of given streams
     */
    fun mirrorOutput(vararg outs: OutputStream): OutputStream {
        return object : OutputStream() {
            override fun write(b: Int) = outs.forEach { it.write(b) }

            override fun write(b: ByteArray?) = outs.forEach { it.write(b) }

            override fun write(b: ByteArray?, off: Int, len: Int) = outs.forEach { it.write(b, off, len) }

            override fun flush() = outs.forEach { it.flush() }

            override fun close() = outs.forEach { it.close() }
        }
    }
}
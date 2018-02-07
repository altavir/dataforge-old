package hep.dataforge.io.output

import hep.dataforge.meta.Meta
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * A stream wrapping the output object. Used for backward compatibility
 */
class StreamConsumer(val output: Output, val meta: Meta = Meta.empty()) : OutputStream() {
    val buffer = ByteArrayOutputStream()

    override fun write(b: Int) {
        synchronized(buffer) {
            buffer.write(b)
            if (b.toChar() == '\n') {
                flush()
            }
        }
    }

    override fun flush() {
        synchronized(buffer) {
            output.push(String(buffer.toByteArray(), Charsets.UTF_8), meta)
            buffer.reset()
        }
    }

    override fun close() {
        flush()
    }
}
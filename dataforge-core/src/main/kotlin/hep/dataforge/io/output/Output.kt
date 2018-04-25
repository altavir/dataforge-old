package hep.dataforge.io.output

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.TaglessEnvelopeType.Companion.TAGLESS_ENVELOPE_TYPE
import hep.dataforge.io.history.Record
import hep.dataforge.io.markup.Markedup
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.io.markup.SimpleMarkupRenderer
import hep.dataforge.kodex.asMap
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.FileReference
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.io.PrintWriter

/**
 * An interface for generic display capabilities
 */
interface Output {
    /**
     * Display an object with given configuration. Throw an exception if object type not supported
     */
    fun push(obj: Any, meta: Meta = Meta.empty())

    /**
     * Clear current content of display if it is possible
     */
    fun clear()

    companion object {
        fun splitOutput(vararg outputs: Output): Output {
            return object : Output {
                override fun push(obj: Any, meta: Meta) {
                    outputs.forEach { it.push(obj, meta) }
                }

                override fun clear() {
                    outputs.forEach { it.clear() }
                }

            }
        }

        fun fileOutput(ref: FileReference): Output {
            return FileOutput(ref)
        }

        fun streamOutput(stream: OutputStream): Output {
            return StreamOutput(stream)
        }
    }
}

/**
 * A display based on OutputStream. The stream must be closed by caller
 */
open class StreamOutput(val stream: OutputStream) : Output, AutoCloseable {
    private val printer = PrintWriter(stream)
    private val renderer = SimpleMarkupRenderer(stream)

    private val logEncoder: Encoder<ILoggingEvent> by lazy {
        PatternLayoutEncoder().apply {
            pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
            context = LoggerFactory.getILoggerFactory() as LoggerContext
            start()
        }
    }

    override fun push(obj: Any, meta: Meta) {
        //TODO use context dispatch stream or something like that
        synchronized(printer) {
            when (obj) {
                is Markup -> renderer.render(obj)
                is MarkupBuilder -> renderer.render(obj)
                is Markedup -> renderer.render(obj.markup(meta))
                is Envelope -> {
                    val envelopeType = EnvelopeType.resolve(meta.getString("envelope.type", TAGLESS_ENVELOPE_TYPE))!!
                    val envelopeProperties = meta.getMeta("envelope.properties", Meta.empty()).asMap { it.string }
                    envelopeType.getWriter(envelopeProperties).write(stream, obj)
                }
                is ILoggingEvent -> {
                    printer.println(String(logEncoder.encode(obj)))
                    //printer.println("${obj.loggerName} [${obj.level}] : ${obj.formattedMessage}")
                }
                is CharSequence -> printer.println(obj)
                is Record -> printer.println(obj)
                else -> printer.println(obj)
            }
            printer.flush()
        }
    }

    override fun clear() {
        // clear not supported
    }

    override fun close() {
        stream.close()
    }
}

class FileOutput(val file: FileReference) : Output, AutoCloseable {
    private val streamOutput by lazy {
        StreamOutput(file.outputStream)
    }

    override fun push(obj: Any, meta: Meta) {
        streamOutput.push(obj, meta)
    }

    /**
     * Delete the output file
     */
    override fun clear() {
        close()
        file.delete()
    }

    override fun close() {
        streamOutput.close()
    }

}
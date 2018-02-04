package hep.dataforge.io.display

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.markup.Markedup
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.io.markup.SimpleMarkupRenderer
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
                is Envelope ->{

                }
                is ILoggingEvent -> {
                    printer.println(String(logEncoder.encode(obj)))
                    //printer.println("${obj.loggerName} [${obj.level}] : ${obj.formattedMessage}")
                }
                is CharSequence -> printer.println(obj)
            //TODO add record formatter
                else -> printer.println(obj.toString())
            }
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
    override fun push(obj: Any, meta: Meta) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
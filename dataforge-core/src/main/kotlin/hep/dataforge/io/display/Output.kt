package hep.dataforge.io.display

import ch.qos.logback.classic.spi.ILoggingEvent
import hep.dataforge.io.markup.Markedup
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.io.markup.SimpleMarkupRenderer
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.FileReference
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
        fun splitDisplay(vararg outputs: Output): Output {
            return object : Output {
                override fun push(obj: Any, meta: Meta) {
                    outputs.forEach { it.push(obj, meta) }
                }

                override fun clear() {
                    outputs.forEach { it.clear() }
                }

            }
        }

        fun fileDisplay(ref: FileReference): Output {
            return StreamOutput(ref.output)
        }
    }
}

/**
 * A display based on OutputStream. The stream must be closed by caller
 */
open class StreamOutput(val stream: OutputStream) : Output, AutoCloseable {
    private val printer = PrintWriter(stream)
    private val renderer = SimpleMarkupRenderer(stream)

    override fun push(obj: Any, meta: Meta) {
        //TODO use context dispatch stream or something like that
        synchronized(printer) {
            when (obj) {
                is Markup -> renderer.render(obj)
                is MarkupBuilder -> renderer.render(obj)
                is Markedup -> renderer.render(obj.markup(meta))
                is ILoggingEvent -> {
                    printer.println("${obj.loggerName} [${obj.level}] : ${obj.formattedMessage}")
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
package hep.dataforge.io.output

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.encoder.EncoderBase
import hep.dataforge.Named
import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.description.ValueDescriptor
import hep.dataforge.io.IOUtils
import hep.dataforge.io.IOUtils.*
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.TaglessEnvelopeType.Companion.TAGLESS_ENVELOPE_TYPE
import hep.dataforge.io.history.Record
import hep.dataforge.kodex.asMap
import hep.dataforge.kodex.useValue
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.tables.Table
import hep.dataforge.values.ValueType
import hep.dataforge.workspace.FileReference
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.io.PrintWriter
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.reflect.KClass

/**
 * An interface for generic display capabilities
 */
interface Output : ContextAware {
    /**
     * Display an object with given configuration. Throw an exception if object type not supported
     */
    fun render(obj: Any, meta: Meta = Meta.empty())

    /**
     * Clear current content of display if it is possible
     */
    fun clear()

    companion object {
        fun splitOutput(vararg outputs: Output): Output {
            val context = outputs.first().context
            return object : Output {
                override val context: Context
                    get() = context

                override fun render(obj: Any, meta: Meta) {
                    outputs.forEach { it.render(obj, meta) }
                }

                override fun clear() {
                    outputs.forEach { it.clear() }
                }

            }
        }

        fun fileOutput(ref: FileReference): Output {
            return FileOutput(ref)
        }

        fun streamOutput(context: Context, stream: OutputStream): Output {
            return StreamOutput(context, stream)
        }
    }
}

/**
 * The object that knows best how it should be rendered
 */
interface SelfRendered {
    fun render(output: Output, meta: Meta)
}

/**
 * Custom renderer for specific type of object
 */
interface OutputRenderer : Named {
    override val name: String
    val type: KClass<*>
    fun render(output: Output, obj: Any, meta: Meta)
}

/**
 * A n output that could display plain text with attributes
 */
interface TextOutput : Output {
    fun renderText(text: String, vararg attributes: TextAttribute)

    @JvmDefault
    fun renderText(text: String, color: Color) {
        renderText(text, TextColor(color))
    }
}

/**
 * A display based on OutputStream. The stream must be closed by caller
 */
open class StreamOutput(override val context: Context, val stream: OutputStream) : Output, AutoCloseable, TextOutput {
    private val printer = PrintWriter(stream)
    private val executor = Executors.newSingleThreadExecutor()

    protected open val logEncoder: Encoder<ILoggingEvent> by lazy {
        PatternLayoutEncoder().apply {
            this.pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
            this.context = LoggerFactory.getILoggerFactory() as LoggerContext
            start()
        }
    }

    override fun render(obj: Any, meta: Meta) {
        executor.run {
            meta.useValue("text.offset") { repeat(it.int) { renderText("\t") } }
            meta.useValue("text.bullet") { renderText(it.string + " ") }
            when (obj) {
                is Meta -> renderMeta(obj, meta)
                is SelfRendered -> {
                    obj.render(this@StreamOutput, meta)
                }
                is Table -> {
                    //TODO add support for tab-stops
                    renderText(obj.format.names.joinToString(separator = "\t"), TextColor(Color.BLUE))
                    obj.rows.forEach { values ->
                        printer.println(obj.format.names.map { values[it] }.joinToString(separator = "\t"))
                    }
                }
                is Envelope -> {
                    val envelopeType = EnvelopeType.resolve(meta.getString("envelope.encoding", TAGLESS_ENVELOPE_TYPE))
                            ?: throw RuntimeException("Unknown envelope encoding")
                    val envelopeProperties = meta.getMeta("envelope.properties", Meta.empty()).asMap { it.string }
                    envelopeType.getWriter(envelopeProperties).write(stream, obj)
                }
                is ILoggingEvent -> {
                    printer.println(String(logEncoder.encode(obj)))
                }
                is CharSequence -> printer.println(obj)
                is Record -> printer.println(obj)
                is ValueDescriptor -> {
                    if (obj.isRequired) renderText("(*) ", Color.CYAN)
                    renderText(obj.name, Color.RED)
                    if (obj.isMultiple) renderText(" (mult)", Color.CYAN)
                    renderText(" (${obj.type().first()})")
                    if (obj.hasDefault()) {
                        val def = obj.defaultValue()
                        if (def.type == ValueType.STRING) {
                            renderText(" = \"")
                            renderText(def.string, Color.YELLOW)
                            renderText("\": ")
                        } else {
                            renderText(" = ")
                            renderText(def.string, Color.YELLOW)
                            renderText(": ")
                        }
                    } else {
                        renderText(": ")
                    }
                    renderText(obj.info)
                }
                is NodeDescriptor -> {
                    obj.childrenDescriptors().forEach { key, value ->
                        val newMeta = meta.builder
                                .setValue("text.offset", meta.getInt("text.offset", 0) + 1)
                                .setValue("text.bullet", "+")
                        renderText(key + "\n", Color.BLUE)
                        if (value.isRequired) renderText("(*) ", Color.CYAN)

                        renderText(value.name, Color.MAGENTA)

                        if (value.isMultiple) renderText(" (mult)", Color.CYAN)

                        if (!value.info.isEmpty()) {
                            renderText(": ${value.info}")
                        }
                        render(value, newMeta)
                    }

                    obj.valueDescriptors().forEach { key, value ->
                        val newMeta = meta.builder
                                .setValue("text.offset", meta.getInt("text.offset", 0) + 1)
                                .setValue("text.bullet", "-")
                        renderText(key + "\n", Color.BLUE)
                        render(value, newMeta)
                    }
                }
                is Metoid -> {
                    val renderType = obj.meta.getString("@output.type", "@default")
                    context.findService(OutputRenderer::class.java) { it.name == renderType }
                            ?.render(this@StreamOutput, obj, meta)
                            ?: renderMeta(obj.meta, meta)
                }
                else -> printer.println(obj)
            }
            printer.flush()
        }
    }

    open fun renderMeta(meta: Meta, options: Meta) {
        printer.println(meta.toString())
    }

    override fun renderText(text: String, vararg attributes: TextAttribute) {
        printer.println(text)
    }

    override fun clear() {
        // clear not supported
    }

    override fun close() {
        stream.close()
    }
}

/**
 * A stream output with ANSI colors enabled
 */
class ANSIStreamOutput(context: Context, stream: OutputStream) : StreamOutput(context, stream) {

    override val logEncoder: Encoder<ILoggingEvent> by lazy {
        object : EncoderBase<ILoggingEvent>() {
            override fun headerBytes(): ByteArray = ByteArray(0)

            override fun footerBytes(): ByteArray = ByteArray(0)

            override fun encode(event: ILoggingEvent): ByteArray {
                return buildString {
                    append(Instant.ofEpochMilli(event.timeStamp).toString() + "\t")
                    //%level [%thread] %logger{10} [%file:%line] %msg%n
                    if (event.threadName != Thread.currentThread().name) {
                        append("[${event.threadName}]\t")
                    }
                    append(wrapANSI(event.loggerName, ANSI_BLUE) + "\t")

                    when (event.level) {
                        Level.ERROR -> appendln(wrapANSI(event.message, ANSI_RED))
                        Level.WARN -> appendln(wrapANSI(event.message, ANSI_YELLOW))
                        else -> appendln(event.message)
                    }
                }.toByteArray()
            }

        }.apply {
            this.context = LoggerFactory.getILoggerFactory() as LoggerContext
            start()
        }
    }

    private fun wrapText(text: String, vararg attributes: TextAttribute): String {
        return attributes.find { it is TextColor }?.let {
            when ((it as TextColor).color) {
                Color.BLACK -> IOUtils.wrapANSI(text, ANSI_BLACK)
                Color.RED -> IOUtils.wrapANSI(text, ANSI_RED)
                Color.GREEN -> IOUtils.wrapANSI(text, ANSI_GREEN)
                Color.YELLOW -> IOUtils.wrapANSI(text, ANSI_YELLOW)
                Color.BLUE -> IOUtils.wrapANSI(text, ANSI_BLUE)
                Color.PURPLE -> IOUtils.wrapANSI(text, ANSI_PURPLE)
                Color.CYAN -> IOUtils.wrapANSI(text, ANSI_CYAN)
                Color.WHITE -> IOUtils.wrapANSI(text, ANSI_WHITE)
                else -> {
                    //Color is not resolved
                    text
                }
            }
        } ?: text
    }

    override fun renderText(text: String, vararg attributes: TextAttribute) {
        super.renderText(wrapText(text, *attributes), *attributes)
    }
}

class FileOutput(val file: FileReference) : Output, AutoCloseable {
    override val context: Context
        get() = file.context

    private val streamOutput by lazy {
        StreamOutput(context, file.outputStream)
    }

    override fun render(obj: Any, meta: Meta) {
        streamOutput.render(obj, meta)
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
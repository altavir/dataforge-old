package hep.dataforge.io

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.FileAppender
import hep.dataforge.context.*
import hep.dataforge.io.output.FileOutput
import hep.dataforge.io.output.Output
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.workspace.FileReference
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A directory based IO manager. Any named output is redirected to file in corresponding directory inside work directory
 */
@PluginDef(name = "io.dir", group = "hep.dataforge", info = "Directory based output plugin")
class DirectoryIO : DefaultIOManager() {

    //internal var registry = ReferenceRegistry<OutputStream>()
    //    FileAppender<ILoggingEvent> appender;

    private val map = HashMap<Meta, FileOutput>()


    override fun createLoggerAppender(): Appender<ILoggingEvent> {
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        val ple = PatternLayoutEncoder()

        ple.pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
        ple.context = lc
        ple.start()
        val appender = FileAppender<ILoggingEvent>()
        appender.file = File(workDir.toFile(), meta.getString("logFileName", "${context.name}.log")).toString()
        appender.encoder = ple
        return appender
    }

    override fun detach() {
        super.detach()
        map.values.forEach {
            //TODO add catch
            it.close()
        }
    }

    /**
     * Get file extension for given content type
     */
    private fun getExtension(type: String): String {
        return when (type) {
            IOManager.DEFAULT_OUTPUT_TYPE -> "out"
            else -> type
        }
    }

    override fun output(meta: Meta): Output {
        return map.getOrPut(meta) {
            val name = Name.of(meta.getString("name"))
            val stage = Name.of(meta.getString("stage", ""))
            val type = meta.getString("type", DEFAULT_OUTPUT_TYPE)
            //TODO make scope customizable?
            val reference = FileReference.newWorkFile(context, name.toUnescaped(), getExtension(type), stage)
            FileOutput(reference)
        }
    }



    class Factory: PluginFactory() {
        override val type: Class<out Plugin> = DirectoryIO::class.java

        override fun build(meta: Meta): Plugin {
            return DirectoryIO()
        }
    }

}
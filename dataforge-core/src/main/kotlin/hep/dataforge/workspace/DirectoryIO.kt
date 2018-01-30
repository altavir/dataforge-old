package hep.dataforge.workspace

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.FileAppender
import hep.dataforge.context.DefaultIOManager
import hep.dataforge.context.IOManager
import hep.dataforge.names.Name
import hep.dataforge.utils.ReferenceRegistry
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * A directory based IO manager. Any named output is redirected to file in corresponding directory inside work directory
 */
class DirectoryIO : DefaultIOManager() {

    internal var registry = ReferenceRegistry<OutputStream>()
    //    FileAppender<ILoggingEvent> appender;


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
        registry.forEach { it ->
            try {
                it.close()
            } catch (e: IOException) {
                LoggerFactory.getLogger(javaClass).error("Failed to close output", e)
            }
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

    override fun out(stage: Name?, name: Name, type: String): OutputStream {
        val fileReference = FileReference.newWorkFile(context,name.toUnescaped(), getExtension(type), stage?: Name.EMPTY)
        val out = fileReference.output
        registry.add(out)
        return out
    }
}
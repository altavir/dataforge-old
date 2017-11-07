/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.fragments

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import hep.dataforge.fx.FXUtils
import hep.dataforge.fx.output.FXOutputPane
import javafx.scene.Parent
import org.slf4j.LoggerFactory

import java.io.PrintStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.BiConsumer

/**
 * @author Alexander Nozik
 */
class LogFragment : FXFragment("DataForge output log", 800, 200), AutoCloseable {

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    val outputPane: FXOutputPane
    private var formatter: BiConsumer<FXOutputPane, String>? = null
    private val loggerFormatter = { pane: FXOutputPane, eventObject: ILoggingEvent ->
        val style: String
        when (eventObject.level.toString()) {
            "DEBUG" -> style = "-fx-fill: green"
            "WARN" -> style = "-fx-fill: orange"
            "ERROR" -> style = "-fx-fill: red"
            else -> style = "-fx-fill: black"
        }

        FXUtils.runNow {
            val time = Instant.ofEpochMilli(eventObject.timeStamp)
            pane.append(timeFormatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault())) + ": ")

            pane.appendColored(eventObject.loggerName, "gray")

            pane.appendStyled(eventObject.formattedMessage.replace("\n", "\n\t") + "\r\n", style)
        }

    }

    private val logAppender: Appender<ILoggingEvent>
    private var stdHooked = false

    init {
        outputPane = FXOutputPane()

        outputPane.setMaxLines(2000)
        logAppender = object : AppenderBase<ILoggingEvent>() {
            override fun append(eventObject: ILoggingEvent) {
                synchronized(this@LogFragment) {
                    loggerFormatter.accept(outputPane, eventObject)
                }
            }
        }
        logAppender.setName(FX_LOG_APPENDER_NAME)
        //        logAppender.setContext(Global.instance().getLogger().getLoggerContext());
        logAppender.start()
    }

    /**
     * Set custom formatter for text
     *
     * @param formatter
     */
    fun setFormatter(formatter: BiConsumer<FXOutputPane, String>) {
        this.formatter = formatter
    }

    fun appendText(text: String) {
        if (formatter == null) {
            outputPane.append(text)
        } else {
            formatter!!.accept(outputPane, text)
        }
    }

    fun appendLine(text: String) {
        appendText(text + "\r\n")
    }

    fun addRootLogHandler() {
        addLogHandler(Logger.ROOT_LOGGER_NAME)
    }

    fun addLogHandler(loggerName: String) {
        addLogHandler(LoggerFactory.getLogger(loggerName))
    }

    fun addLogHandler(logger: org.slf4j.Logger) {
        if (logger is Logger) {
            logger.addAppender(logAppender)
        } else {
            LoggerFactory.getLogger(javaClass).error("Failed to add log handler. Only Logback loggers are supported.")
        }

    }

    /**
     * Redirect copy of std streams to this console window
     */
    @Deprecated("")
    fun hookStd() {
        if (!stdHooked) {
            //            System.setOut(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_OUT)));
            //            System.setErr(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_ERR)));
            System.setOut(PrintStream(outputPane.stream))
            System.setErr(PrintStream(outputPane.stream))
            stdHooked = true
        }
    }

    /**
     * Restore default std streams
     */
    @Deprecated("")
    fun restoreStd() {
        if (stdHooked) {
            System.setOut(STD_OUT)
            System.setErr(STD_ERR)
            stdHooked = false
        }
    }

    override fun close() {
        logAppender.stop()
    }

    public override fun buildRoot(): Parent {
        return outputPane.root
    }

    companion object {

        private val STD_OUT = System.out
        private val STD_ERR = System.err

        private val FX_LOG_APPENDER_NAME = "hep.dataforge.fx"
    }

}

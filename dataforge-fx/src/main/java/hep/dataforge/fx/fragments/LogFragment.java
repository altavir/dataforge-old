/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.fragments;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import hep.dataforge.fx.FXDataOutputPane;
import hep.dataforge.fx.FXUtils;
import javafx.scene.Parent;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.time.Instant;
import java.util.function.BiConsumer;

/**
 * @author Alexander Nozik
 */
public class LogFragment extends Fragment implements AutoCloseable {

    public final static PrintStream STD_OUT = System.out;
    public final static PrintStream STD_ERR = System.err;

    public static final String CONSOLE_LOG_APPENDER_NAME = "hep.dataforge.fx.ConsoleWindow";

    private FXDataOutputPane outputPane;
    private BiConsumer<FXDataOutputPane, String> formatter;
    private BiConsumer<FXDataOutputPane, ILoggingEvent> loggerFormatter = (FXDataOutputPane pane, ILoggingEvent eventObject) -> {
        String style;
        switch (eventObject.getLevel().toString()) {
            case "DEBUG":
                style = "-fx-color: green";
                break;
            case "WARN":
                style = "-fx-color: orange";
                break;
            case "ERROR":
                style = "-fx-color: red";
                break;
            default:
                style = "-fx-color: black";
        }

        FXUtils.runNow(() -> {
            pane.appendColored(Instant.ofEpochMilli(eventObject.getTimeStamp()).toString() + ": ", "gray");
            pane.appendStyled(eventObject.getFormattedMessage().replace("\n", "\n\t") + "\r\n", style);
        });

    };

    private final Appender<ILoggingEvent> logAppender;
    private boolean stdHooked = false;

    public LogFragment() {
        super("DataForge console", 800, 200);
        outputPane = new FXDataOutputPane();

        outputPane.setMaxLines(2000);
        logAppender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                synchronized (LogFragment.this) {
                    loggerFormatter.accept(outputPane, eventObject);
                }
            }
        };
        logAppender.setName(CONSOLE_LOG_APPENDER_NAME);
//        logAppender.setContext(Global.instance().getLogger().getLoggerContext());
        logAppender.start();
    }

    /**
     * Set custom formatter for text
     *
     * @param formatter
     */
    public void setFormatter(BiConsumer<FXDataOutputPane, String> formatter) {
        this.formatter = formatter;
    }

    public void appendText(String text) {
        if (formatter == null) {
            outputPane.append(text);
        } else {
            formatter.accept(outputPane, text);
        }
    }

    public void appendLine(String text) {
        appendText(text + "\r\n");
    }

    public FXDataOutputPane getOutputPane() {
        return outputPane;
    }

    public void addRootLogHandler() {
        addLogHandler(Logger.ROOT_LOGGER_NAME);
    }

    public void addLogHandler(String loggerName) {
        addLogHandler(LoggerFactory.getLogger(loggerName));
    }

    public void addLogHandler(org.slf4j.Logger logger) {
        if (logger instanceof Logger) {
            ((Logger) logger).addAppender(logAppender);
        } else {
            LoggerFactory.getLogger(getClass()).error("Failed to add log handler. Only Logback loggers are supported.");
        }

    }

    /**
     * Redirect copy of std streams to this console window
     */
    public void hookStd() {
        if (!stdHooked) {
            System.setOut(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_OUT)));
            System.setErr(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_ERR)));
            stdHooked = true;
        }
    }

    /**
     * Restore default std streams
     */
    public void restoreStd() {
        if (stdHooked) {
            System.setOut(STD_OUT);
            System.setErr(STD_ERR);
            stdHooked = false;
        }
    }

    @Override
    public void close() {
        restoreStd();
        logAppender.stop();
    }

    @Override
    public Parent buildRoot() {
        return outputPane.getRoot();
    }

}

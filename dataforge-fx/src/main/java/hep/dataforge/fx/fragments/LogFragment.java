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
import hep.dataforge.fx.FXUtils;
import hep.dataforge.fx.output.FXOutputPane;
import javafx.scene.Parent;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

/**
 * @author Alexander Nozik
 */
public class LogFragment extends Fragment implements AutoCloseable {

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    private final static PrintStream STD_OUT = System.out;
    private final static PrintStream STD_ERR = System.err;

    private static final String FX_LOG_APPENDER_NAME = "hep.dataforge.fx";

    private FXOutputPane outputPane;
    private BiConsumer<FXOutputPane, String> formatter;
    private BiConsumer<FXOutputPane, ILoggingEvent> loggerFormatter = (FXOutputPane pane, ILoggingEvent eventObject) -> {
        String style;
        switch (eventObject.getLevel().toString()) {
            case "DEBUG":
                style = "-fx-fill: green";
                break;
            case "WARN":
                style = "-fx-fill: orange";
                break;
            case "ERROR":
                style = "-fx-fill: red";
                break;
            default:
                style = "-fx-fill: black";
        }

        FXUtils.runNow(() -> {
            Instant time = Instant.ofEpochMilli(eventObject.getTimeStamp());
            pane.appendColored(timeFormatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault())) + ": ", "gray");
            pane.appendStyled(eventObject.getFormattedMessage().replace("\n", "\n\t") + "\r\n", style);
        });

    };

    private final Appender<ILoggingEvent> logAppender;
    private boolean stdHooked = false;

    public LogFragment() {
        super("DataForge output log", 800, 200);
        outputPane = new FXOutputPane();

        outputPane.setMaxLines(2000);
        logAppender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                synchronized (LogFragment.this) {
                    loggerFormatter.accept(outputPane, eventObject);
                }
            }
        };
        logAppender.setName(FX_LOG_APPENDER_NAME);
//        logAppender.setContext(Global.instance().getLogger().getLoggerContext());
        logAppender.start();
    }

    /**
     * Set custom formatter for text
     *
     * @param formatter
     */
    public void setFormatter(BiConsumer<FXOutputPane, String> formatter) {
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

    public FXOutputPane getOutputPane() {
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
//            System.setOut(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_OUT)));
//            System.setErr(new PrintStream(new TeeOutputStream(outputPane.getStream(), STD_ERR)));
            System.setOut(new PrintStream(outputPane.getStream()));
            System.setErr(new PrintStream(outputPane.getStream()));
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import hep.dataforge.context.GlobalContext;
import java.io.PrintStream;
import java.util.function.BiConsumer;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class ConsoleFragment extends FXFragment implements AutoCloseable {

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
                style = "";
        }

        pane.appendStyled(eventObject.getFormattedMessage() + "\r\n", style);
    };

    private final Appender<ILoggingEvent> logAppender;
    private boolean stdHooked = false;

    public ConsoleFragment() {
        outputPane = new FXDataOutputPane();

        outputPane.setMaxLines(2000);
        logAppender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                loggerFormatter.accept(outputPane, eventObject);
            }
        };
        logAppender.setName(CONSOLE_LOG_APPENDER_NAME);
        logAppender.setContext(GlobalContext.instance().getLogger().getLoggerContext());
        logAppender.start();
    }

    @Override
    protected Stage buildStage() {
        Stage stage = new Stage();
        stage.setTitle("DataForge console");
        stage.setScene(new Scene(outputPane.getHolder(), 800, 200));
        stage.sizeToScene();

        return stage;
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
        try {
            addLogHandler((Logger) LoggerFactory.getLogger(loggerName));
        } catch (ClassCastException ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to add log handler. Only Logback loggers are supported.");
        }
    }

    public void addLogHandler(Logger logger) {
        logger.addAppender(logAppender);
    }

    /**
     * Redirect copy of std streams to this console window
     */
    public void hookStd() {
        if (!stdHooked) {
            System.setOut(new PrintStream(new TeeOutputStream(outputPane.getOutputStream(), STD_OUT)));
            System.setErr(new PrintStream(new TeeOutputStream(outputPane.getOutputStream(), STD_ERR)));
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
        super.close();
    }

}

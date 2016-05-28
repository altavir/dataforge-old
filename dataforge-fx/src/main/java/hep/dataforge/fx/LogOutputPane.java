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
import static hep.dataforge.fx.ConsoleFragment.CONSOLE_LOG_APPENDER_NAME;
import hep.dataforge.io.reports.ReportEntry;
import hep.dataforge.io.reports.Reportable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.LoggerFactory;

/**
 * A textFlow used to represent work logs
 *
 * @author Alexander Nozik
 */
@Deprecated
public class LogOutputPane extends ScrollPane implements Consumer<ReportEntry> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final Appender<ILoggingEvent> logAppender;
    private final TextFlow flow;

    public LogOutputPane() {
        this.flow = new TextFlow();
//        flow.setLineSpacing(5);
        setPadding(new Insets(5));
        setContent(flow);
        logAppender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                Text event = new Text(eventObject.getFormattedMessage() + "\r\n");
                event.setFill(Color.RED);
                Platform.runLater(() -> flow.getChildren().add(event));
            }
        };
        logAppender.setName(CONSOLE_LOG_APPENDER_NAME);
        logAppender.setContext(GlobalContext.instance().getLogger().getLoggerContext());
        logAppender.start();
    }

    @Override
    public void accept(ReportEntry t) {
        Text time = new Text(formatter.format(t.getTime()) + " ");
        time.setFill(Color.GREY);
        Text trace = new Text(t.getTraceString() + ": ");
        trace.setFill(Color.BLUE);
        Text message = new Text(t.getMessage() + "\r\n");
        Platform.runLater(() -> flow.getChildren().addAll(time, trace, message));
    }

    /**
     * Set this logOutputPane as listener to given logable
     *
     * @param logable
     */
    public void listenTo(Reportable logable) {
        logable.getReport().addReportListener(this);
    }
    
    public void addLogHandler(String loggerName) {
        try {
            addLogHandler((Logger) LoggerFactory.getLogger(loggerName));
        } catch (ClassCastException ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to add log handler. Only Logback loggers are supported.");
        }
    }
    
    public void addRootLogHandler() {
        addLogHandler(Logger.ROOT_LOGGER_NAME);
    }    

    public void addLogHandler(Logger logger) {
        logger.addAppender(logAppender);
    }
    
    public void clear() {
        Platform.runLater(() -> flow.getChildren().clear());
    }
}

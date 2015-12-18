/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import hep.dataforge.io.log.LogEntry;
import hep.dataforge.io.log.Logable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A textFlow used to represent work logs
 * @author Alexander Nozik
 */
public class LogOutputPane extends ScrollPane implements Consumer<LogEntry> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final TextFlow flow;

    public LogOutputPane() {
        this.flow = new TextFlow();
//        flow.setLineSpacing(5);
        setPadding(new Insets(5));
        setContent(flow);
    }
    
    

    @Override
    public void accept(LogEntry t) {
        Text time = new Text(formatter.format(t.getTime()) + " ");
        time.setFill(Color.GREY);
        Text trace = new Text(t.getTraceString() + ": ");
        trace.setFill(Color.BLUE);
        Text message = new Text(t.getMessage() + "\r\n");
        Platform.runLater(() -> flow.getChildren().addAll(time, trace, message));
    }
    
    /**
     * Set this logOutputPane as listener to given logable
     * @param logable 
     */
    public void attachLog(Logable logable){
        logable.getLog().setLogListener(this);
    }
    
    /**
     * Get logback appender that appends to this pane
     * @return 
     */
    public Appender<ILoggingEvent> getLoggerAppender(){
        return new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                Text event = new Text(eventObject.getFormattedMessage());
                event.setFill(Color.RED);
                Platform.runLater(()->flow.getChildren().add(event));
            }
        };
    }
    
    public void clear(){
        Platform.runLater(()->flow.getChildren().clear());
    }
}

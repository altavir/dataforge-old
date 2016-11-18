/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.io.reports.LogEntry;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 *
 * @author Alexander Nozik
 */
public class FXReportListener implements Consumer<LogEntry> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final FXDataOutputPane pane;

    public FXReportListener(FXDataOutputPane pane) {
        this.pane = pane;
    }

    @Override
    public synchronized void accept(LogEntry t) {
        FXUtils.runNow(() -> {
            pane.appendColored(FORMATTER.format(t.getTime()) + " ", "grey");
            pane.appendColored(t.getTraceString() + ": ", "blue");
            pane.appendColored(t.getMessage(), "black");
            pane.newline();
        });
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.output

import hep.dataforge.io.history.Record
import tornadofx.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

/**
 *
 * @author Alexander Nozik
 */
class FXReportListener(private val pane: FXOutputPane) : Consumer<Record> {

    @Synchronized override fun accept(t: Record) {
        runLater {
            pane.appendColored(FORMATTER.format(t.time) + " ", "grey")
            pane.appendColored(t.traceString + ": ", "blue")
            pane.appendColored(t.message, "black")
            pane.newline()
        }
    }

    companion object {

        private val FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())
    }

}

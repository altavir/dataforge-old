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
class FXReportListener(private val text: FXTextOutput) : Consumer<Record> {

    @Synchronized override fun accept(t: Record) {
        runLater {
            text.appendColored(FORMATTER.format(t.time) + " ", "grey")
            text.appendColored(t.traceString + ": ", "blue")
            text.appendColored(t.message, "black")
            text.newline()
        }
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())
    }

}

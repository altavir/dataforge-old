package hep.dataforge.plots.demo

import hep.dataforge.kodex.fx.TableDisplay
import hep.dataforge.tables.ListTable
import javafx.application.Application
import tornadofx.*


class TableDisplayTest: App(TableDisplayView::class) {
}

class TableDisplayView: View() {
    private val table = ListTable.Builder("x","y")
            .row(1,1)
            .row(2,2)
            .row(3,3)
            .build()

    override val root = TableDisplay(table).root

}

fun main(args: Array<String>) {
    Application.launch(TableDisplayTest::class.java,*args);
}

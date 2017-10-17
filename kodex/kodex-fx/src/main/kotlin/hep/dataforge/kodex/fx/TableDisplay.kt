package hep.dataforge.kodex.fx

import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.tables.Table
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import org.controlsfx.control.spreadsheet.*
import tornadofx.*

/**
 * Table display fragment
 */
class TableDisplay(val table: Table, meta: Meta = Meta.empty())
    : Fragment(title = meta.optString("title").orElse(null), icon = ImageView(dfIcon)), Metoid {

    private fun buildCell(row: Int, column: Int, value: Value): SpreadsheetCell {
        return when (value.type) {
            ValueType.NUMBER -> SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, value.doubleValue())
            else -> SpreadsheetCellType.STRING.createCell(row, column, 1, 1, value.stringValue())
        }
    }


    private val grid: Grid = GridBase(table.size(), table.format.count()).apply {
        val format = table.format;

        columnHeaders.setAll(format.names.asList())
//        rows += format.names.asList().observable();

        (0 until table.size()).forEach { i ->
            rows += (0 until format.count())
                    .map { j -> buildCell(i, j, table.get(format.names[j], i)) }
                    .observable()
        }
    }

    private val spreadsheet = CustomSpreadSheetView(grid).apply {
        isEditable = false
//        isShowColumnHeader = false
    }

    override val root = borderpane {
        center = spreadsheet;
    }

    override fun meta(): Meta {
        return meta
    }

    class CustomSpreadSheetView(grid: Grid) : SpreadsheetView(grid) {
        override fun copyClipboard() {
            val posList = selectionModel.selectedCells

            val columns = posList.map { it.column }.distinct().sorted()
            val rows = posList.map { it.row }.distinct().sorted()


            //building text
            val text = rows.joinToString(separator = "\n") { row ->
                columns.joinToString(separator = "\t") { column ->
                    grid.rows[row][column].text
                }
            }

            //TODO add HTML binding

            val content = ClipboardContent()
            content.putString(text);
//            content.put(DataFormat("SpreadsheetView"), list)
            Clipboard.getSystemClipboard().setContent(content)
        }
        //TODO add pasteClipboard
    }
}

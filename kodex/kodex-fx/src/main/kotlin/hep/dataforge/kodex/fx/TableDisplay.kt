package hep.dataforge.kodex.fx

import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.tables.Table
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import org.controlsfx.control.spreadsheet.*
import tornadofx.*

class TableDisplay(val table: Table, meta: Meta = Meta.empty()) : Fragment(meta.optString("title").orElse(null)), Metoid {

    private fun buildCell(row: Int, column: Int, value: Value): SpreadsheetCell {
        return when (value.type) {
            ValueType.NUMBER -> SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, value.doubleValue())
            else -> SpreadsheetCellType.STRING.createCell(row, column, 1, 1, value.stringValue())
        }
    }


    private val grid: Grid = GridBase(table.size(), table.format.count()).apply {
        val format = table.format;

        for (i in (1..table.size())) {
            rows += (1..format.count())
                    .map { j -> buildCell(i, j, table.get(format.names[j - 1], i - 1)) }
                    .observable()
        }
    }

    private val spreadsheet: SpreadsheetView = SpreadsheetView(grid)

    override val root = borderpane {
        center = spreadsheet;
    }

    override fun meta(): Meta {
        return meta
    }
}

package hep.dataforge.fx.output

import hep.dataforge.io.markup.GenericMarkupRenderer
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.RowMarkup

/**
 * An FX panel markup renderer
 * Created by darksnake on 19-Mar-17.
 */
class FXMarkupRenderer(private val out: FXOutputPane) : GenericMarkupRenderer() {


    override fun text(text: String, color: String?, element: Markup) {
        if (color == null) {
            out.append(text)
        } else {
            out.appendColored(text, color)
        }
    }

    override fun listItem(level: Int, bullet: String, element: Markup) {
        out.newline()
        for (i in 0 until level) {
            out.tab()
        }
        out.append(bullet)
        doRender(element)
    }


    override fun tableRow(element: RowMarkup, isHeader: Boolean) {
        element.content.forEach { cell ->
            doRender(cell)
            out.tab()
        }

        if (isHeader) {
            out.newline()
        }

        out.newline()
    }
}

package hep.dataforge.fx.output

import hep.dataforge.io.markup.GenericMarkupRenderer
import hep.dataforge.io.markup.Markup

/**
 * An FX panel markup renderer
 * Created by darksnake on 19-Mar-17.
 */
class FXMarkupRenderer(private val out: FXOutputPane) : GenericMarkupRenderer() {


    override fun renderText(text: String, color: String, element: Markup) {
        out.appendColored(text, color)
    }

    override fun listItem(level: Int, bullet: String, element: Markup) {
        out.newline()
        for (i in 0 until level) {
            out.tab()
        }
        out.append(bullet)
        doRender(element)
    }

    override fun tableRow(element: Markup) {
        element.content.forEach { cell ->
            doRender(cell)
            out.tab()
        }

        if (element.getBoolean("header", false)!!) {
            out.newline()
        }

        out.newline()
    }
}

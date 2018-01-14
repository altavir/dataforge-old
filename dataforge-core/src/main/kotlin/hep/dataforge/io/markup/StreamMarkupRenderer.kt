package hep.dataforge.io.markup

import hep.dataforge.io.IOUtils

/**
 * Created by darksnake on 05-Jan-17.
 */
abstract class StreamMarkupRenderer : GenericMarkupRenderer() {

    internal var lineStack = false

    protected abstract fun printText(string: String)

    protected fun print(string: String) {
        if (!string.isEmpty()) {
            lineStack = false
        }
        printText(string)
    }

    /**
     * New line ignoring stacking
     */
    protected abstract fun ln()

    /**
     * New line with ability to stack together
     *
     * @param stack
     */
    protected fun ln(stack: Boolean) {
        if (stack) {
            if (!lineStack) {
                ln()
                lineStack = true
            }
        } else {
            ln()
            lineStack = false
        }
    }

    /**
     * Pre-format text using element meta
     *
     * @param string
     * @param element
     * @return
     */
    protected fun format(string: String, element: Markup): String {
        return IOUtils.formatWidth(string, element.style.getInt("textWidth"))
    }

    @Synchronized override fun text(text: String, color: String, element: Markup) {
        print(format(text, element))
    }

    override fun list(element: Markup) {
        super.list(element)
        ln(true)
    }

    override fun listItem(level: Int, bullet: String, element: Markup) {
        ln(true)
        for (i in 0 until level) {
            print("\t")
        }
        print(bullet)
        doRender(element)
    }

    override fun tableRow(element: Markup) {
        element.content.forEach { cell ->
            doRender(cell)
            print("\t")
        }

        if (element.getBoolean("header", false)) {
            ln(false)
        }

        ln(true)
    }
}

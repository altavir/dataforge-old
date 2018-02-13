package hep.dataforge.io.markup

import hep.dataforge.description.ValueDef
import hep.dataforge.io.markup.Markup.Companion.LIST_TYPE
import hep.dataforge.values.ValueType.BOOLEAN
import org.slf4j.LoggerFactory

/**
 * A basic renderer framework allowing to render basic markup elements: text, list and table
 * Created by darksnake on 03-Jan-17.
 */
abstract class GenericMarkupRenderer : MarkupRenderer {

    /**
     * Called once per render
     *
     * @param mark
     */
    override fun render(mark: Markup) {
        doRender(mark)
    }

    /**
     * Override this method to change internal rendering mechanism. This method is recursively called inside rendering procedure
     *
     * @param element
     */
    protected fun doRender(element: Markup) {
        when (element) {
            is TextMarkup -> text(element)
            is ListMarkup -> list(element)
            is TableMarkup -> table(element)
            is MarkupGroup -> element.content.forEach { this.doRender(it) }//render container
        }
    }

    /**
     * Render element of unknown type. By default logs an error and ignores node
     *
     * @param markup
     */
    protected fun doRenderOther(markup: Markup) {
        LoggerFactory.getLogger(javaClass).error("Unknown markup type: " + markup.type)
    }

    /**
     * @param element
     */
    protected fun text(element: TextMarkup) {
        text(element.text, element.color, element)
    }

    /**
     * Render simple text
     *
     * @param text
     * @param color
     * @param element - additional information about rendered element
     */
    protected abstract fun text(text: String, color: String, element: Markup)

    /**
     * Render list of elements
     *
     * @param element
     */
    protected open fun list(element: ListMarkup) {
        val level = getListLevel(element)
        //TODO add numbered lists with auto-incrementing bullets
        element.content.forEach { item -> listItem(level, element.bullet, item) }
    }

    /**
     * Calculate the level of current list using ancestry
     *
     * @param element
     * @return
     */
    private fun getListLevel(element: ListMarkup): Int {
        if (element.styleStack.hasValue("level")) {
            return element.styleStack.getInt("level")
        } else {
            var level = 1
            var parent = element.parent
            while (parent != null) {
                if (parent.type == LIST_TYPE) {
                    if (parent.hasValue("level")) {
                        return parent.getInt("level") + level
                    } else {
                        level++
                    }
                }
                parent = parent.parent
            }
            return level
        }
    }

    /**
     * List item
     *
     * @param level
     * @param bullet
     * @param element
     */
    protected abstract fun listItem(level: Int, bullet: String, element: Markup)


    protected open fun table(element: TableMarkup) {
        //TODO add header here
        element.content.forEach { this.tableRow(it) }
    }

    /**
     * Table row
     *
     * @param element
     */
    @ValueDef(name = "header", type = [BOOLEAN], info = "If true the row is considered to be a header")
    protected abstract fun tableRow(element: RowMarkup)


    //    /**
    //     * Header
    //     * @param element
    //     */
    //    @ValueDef(name = "level", type = "NUMBER", info = "the level of header")
    //    protected abstract void header(Markup element);
}

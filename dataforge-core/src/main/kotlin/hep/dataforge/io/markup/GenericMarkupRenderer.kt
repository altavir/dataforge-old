package hep.dataforge.io.markup

import hep.dataforge.description.ValueDef
import hep.dataforge.io.markup.Markup.Companion.MARKUP_GROUP_TYPE
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
        when (inferType(element)) {
            MARKUP_GROUP_TYPE //render container
            -> element.content.forEach { this.doRender(it) }
            TEXT_TYPE -> text(element)
            LIST_TYPE -> list(element)
            TABLE_TYPE -> table(element)
            else -> doRenderOther(element)
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

    protected fun inferType(element: Markup): String {
        return if (element.hasValue("text")) {
            TEXT_TYPE
        } else {
            Markup.MARKUP_GROUP_TYPE
        }
    }

    /**
     * @param element
     */
    protected fun text(element: Markup) {
        val text = element.getString("text")
        val color = element.getString("color", "")
        text(text, color, element)
        //        //render children
        //        element.getContent().forEach(this::doRender);
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
    protected open fun list(element: Markup) {
        val bullet = element.getString("bullet", "- ")
        val level = getListLevel(element)
        //TODO add numbered lists with auto-incrementing bullets
        element.content.forEach { item -> listItem(level, bullet, item) }
    }

    /**
     * Calculate the level of current list using ancestry
     *
     * @param element
     * @return
     */
    private fun getListLevel(element: Markup): Int {
        if (element.hasValue("level")) {
            return element.getInt("level")!!
        } else {
            var level = 1
            var parent = element.parent
            while (parent != null) {
                if (parent.type == LIST_TYPE) {
                    if (parent.hasValue("level")) {
                        return parent.getInt("level")!! + level
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


    protected open fun table(element: Markup) {
        //TODO add header here
        element.content.forEach { this.tableRow(it) }
    }

    /**
     * Table row
     *
     * @param element
     */
    @ValueDef(name = "header", type = arrayOf(BOOLEAN), info = "If true the row is considered to be a header")
    protected abstract fun tableRow(element: Markup)

    companion object {
        const val TEXT_TYPE = "text"
        const val HEADER_TYPE = "head"
        const val LIST_TYPE = "list"
        const val TABLE_TYPE = "table"
    }

    //    /**
    //     * Header
    //     * @param element
    //     */
    //    @ValueDef(name = "level", type = "NUMBER", info = "the level of header")
    //    protected abstract void header(Markup element);
}

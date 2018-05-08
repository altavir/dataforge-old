package hep.dataforge.markup.markup

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
    protected abstract fun text(text: String, color: String?, element: Markup)

    /**
     * Render list of elements
     *
     * @param element
     */
    protected open fun list(element: ListMarkup) {
        //TODO add numbered lists with auto-incrementing bullets
        element.content.forEach { item ->
            listItem(element.level, element.bullet, item)
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
        element.header?.let {
            tableRow(it, true)
        }
        element.rows.forEach { this.tableRow(it) }
    }

    /**
     * Table row
     *
     * @param element
     */
    //@ValueDef(name = "header", type = [BOOLEAN], info = "If true the row is considered to be a header")
    protected abstract fun tableRow(element: RowMarkup, isHeader: Boolean = false)


    //    /**
    //     * Header
    //     * @param element
    //     */
    //    @ValueDef(name = "level", type = "NUMBER", info = "the level of header")
    //    protected abstract void header(Markup element);
}

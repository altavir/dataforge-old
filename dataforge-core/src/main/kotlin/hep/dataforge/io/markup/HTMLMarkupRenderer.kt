package hep.dataforge.io.markup

import hep.dataforge.meta.MetaUtils
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.PrintStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * The html renderer for markup
 * Created by darksnake on 07-Jan-17.
 */
class HTMLMarkupRenderer(private val stream: PrintStream) : GenericMarkupRenderer() {
    private var document: Document? = null
    private val stack = ArrayDeque<Element>()

    @Throws(ParserConfigurationException::class)
    private fun buildDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.newDocument()
    }

    @Throws(TransformerException::class)
    private fun printDocument() {
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf8")
        transformer.transform(DOMSource(document), StreamResult(stream))
        stream.flush()
    }

    /**
     * Build DOM without attaching it to document
     *
     * @param document
     * @param markup
     * @return
     */
    fun buildDOM(document: Document, markup: Markup): Element {
        this.document = document
        stack.clear()
        val root = document.createElement("body")
        stack.add(root)
        doRender(markup)
        stack.removeLast()
        if (stack.size != 0) {
            LoggerFactory.getLogger(javaClass).warn("Node stack not empty after rendering")
        }
        return root
    }

    override fun render(mark: Markup) {
        try {
            val document = buildDocument()
            document.appendChild(buildDOM(document, mark))
            printDocument()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

    override fun text(text: String, color: String, element: Markup) {
        if (!color.isEmpty()) {
            val textElement = document!!.createElement("font")
            applyHTMLStyle(textElement, element)
            textElement.setAttribute("color", color)
            textElement.textContent = text
            stack.last.appendChild(textElement)
        } else {
            val textElement = document!!.createTextNode(text)
            stack.last.appendChild(textElement)
        }
    }

    override fun list(element: Markup) {
        val listNode = document!!.createElement("ul")
        applyHTMLStyle(listNode, element)
        stack.add(listNode)
        super.list(element)
        stack.removeLast()
        stack.last.appendChild(listNode)
    }

    override fun listItem(level: Int, bullet: String, element: Markup) {
        val listItemNode = document!!.createElement("li")
        applyHTMLStyle(listItemNode, element)
        stack.add(listItemNode)
        doRender(element)
        stack.removeLast()
        stack.last.appendChild(listItemNode)
    }

    override fun table(element: Markup) {
        val tableElement = document!!.createElement("table")
        applyHTMLStyle(tableElement, element)
        stack.add(tableElement)
        super.table(element)
        stack.removeLast()
        stack.last.appendChild(tableElement)
    }

    override fun tableRow(element: Markup) {
        val rowElement = document!!.createElement("tr")
        applyHTMLStyle(rowElement, element)
        stack.add(rowElement)
        element.content.forEach { cell ->
            val cellElement = document!!.createElement(if (element.getBoolean("header", false)) "th" else "td")
            stack.add(cellElement)
            doRender(cell)
            stack.removeLast()
            stack.last.appendChild(cellElement)
        }
        stack.removeLast()
        stack.last.appendChild(rowElement)
    }

    private fun applyHTMLStyle(html: Element, markup: Markup) {
        MetaUtils.valueStream(markup.meta.getMeta("html")).forEach { pair -> html.setAttribute(pair.key, pair.value.stringValue()) }
    }

    //    @Override
    //    protected void header(Markup element) {
    //        Element headerElement = document.createElement("h" + element.getInt("level", 1));
    //        headerElement.setTextContent(element.);
    //        applyHTMLStyle(headerElement, element);
    //        stack.getLast().appendChild(headerElement);
    //    }
}

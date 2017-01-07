package hep.dataforge.io.text;

import hep.dataforge.meta.MetaUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by darksnake on 07-Jan-17.
 */
public class HTMLMarkupRenderer extends GenericMarkupRenderer {
    private Document document;
    private Deque<Element> stack = new ArrayDeque<>();
    private final PrintStream stream;

    public HTMLMarkupRenderer(PrintStream stream) {
        this.stream = stream;
    }

    private Document buildDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private void printDocument() throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf8");
        transformer.transform(new DOMSource(document), new StreamResult(stream));
        stream.flush();
    }

    @Override
    public void render(Markup element) {
        try {
            document = buildDocument();
            stack.clear();
            Element root = document.createElement("body");
            document.appendChild(root);
            stack.add(root);
            doRender(element);
            stack.removeLast();
            if (stack.size() != 0) {
                LoggerFactory.getLogger(getClass()).warn("Node stack not empty after rendering");
            }
            printDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void renderText(String text, String color, Markup element) {
        if (!color.isEmpty()) {
            Element textElement = document.createElement("font");
            applyHTMLStyle(textElement, element);
            textElement.setAttribute("color", color);
            textElement.setTextContent(text);
            stack.getLast().appendChild(textElement);
        } else {
            Node textElement = document.createTextNode(text);
            stack.getLast().appendChild(textElement);
        }
    }

    @Override
    protected void list(Markup element) {
        Element listNode = document.createElement("ul");
        applyHTMLStyle(listNode, element);
        stack.add(listNode);
        super.list(element);
        stack.removeLast();
        stack.getLast().appendChild(listNode);
    }

    @Override
    protected void listItem(int level, String bullet, Markup element) {
        Element listItemNode = document.createElement("li");
        applyHTMLStyle(listItemNode, element);
        stack.add(listItemNode);
        doRender(element);
        stack.removeLast();
        stack.getLast().appendChild(listItemNode);
    }

    @Override
    protected void table(Markup element) {
        Element tableElement = document.createElement("table");
        applyHTMLStyle(tableElement, element);
        stack.add(tableElement);
        super.table(element);
        stack.removeLast();
        stack.getLast().appendChild(tableElement);
    }

    @Override
    protected void tableRow(Markup element) {
        Element rowElement = document.createElement("tr");
        applyHTMLStyle(rowElement, element);
        stack.add(rowElement);
        element.getContent().forEach(cell -> {
            Element cellElement = document.createElement(element.getBoolean("header", false) ? "th" : "td");
            stack.add(cellElement);
            doRender(cell);
            stack.removeLast();
            stack.getLast().appendChild(cellElement);
        });
        stack.removeLast();
        stack.getLast().appendChild(rowElement);
    }

    private void applyHTMLStyle(Element html, Markup markup) {
        if (markup.meta().hasNode("html")) {
            MetaUtils.valueStream(markup.meta().getMeta("html")).forEach(pair -> html.setAttribute(pair.getKey(), pair.getValue().stringValue()));
        }
    }
}

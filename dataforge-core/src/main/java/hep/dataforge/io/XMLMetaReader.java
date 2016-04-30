/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.exceptions.ContentException;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.NamingUtils;
import hep.dataforge.values.NamedValue;
import hep.dataforge.values.Value;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

/**
 * A default reader for XML represented Meta
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class XMLMetaReader implements MetaStreamReader {

    @Override
    public MetaBuilder read(InputStream stream, long length, Charset charset) throws IOException, ParseException {
        try {
            DocumentBuilderFactory factory = newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (charset == null) {
                charset = Charset.forName("UTF-8");
            }

            InputSource source;
            if (length < 0) {
                source = new InputSource(new InputStreamReader(stream, charset.newDecoder()));
            } else {
                byte[] bytes = new byte[(int)length];
                stream.read(bytes);
                source = new InputSource(new ByteArrayInputStream(bytes));
            }

            Element element = builder.parse(source).getDocumentElement();

            return buildNode(element);
        } catch (SAXException | ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private MetaBuilder buildNode(Element element) throws ContentException {

        MetaBuilder res = new MetaBuilder(element.getTagName());
        List<NamedValue> values = getValues(element);
        List<Element> elements = getElements(element);

        for (NamedValue value : values) {
            res.putValue(value.getName(), value.getSourceValue());
        }

        for (Element e : elements) {
            //Оставляем только те элементы, в которых есть что-то кроме текста.
            //Те, в которых только текст уже посчитаны как значения
            if (e.hasAttributes() || e.getElementsByTagName("*").getLength() > 0) {
                res.putNode(buildNode(e).build());
            }
        }

        //записываем значения только если нет наследников
        if (!element.getTextContent().isEmpty() && (element.getElementsByTagName("*").getLength() == 0)) {
            res.putValue(element.getTagName(), element.getTextContent());
        }
        //res.putContent(new AnnotatedData("xmlsource", element));

        return res;
    }

    /**
     * Возвращает список всех подэлементов
     *
     * @param element
     * @return
     */
    private List<Element> getElements(Element element) {
        List<Element> res = new ArrayList<>();
        NodeList nodes = element.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elNode = (Element) nodes.item(i);
            if (elNode.getParentNode().equals(element)) {
                res.add(elNode);
            }
        }
        return res;
    }

    private List<NamedValue> getValues(Element element) {
        List<NamedValue> res = new ArrayList<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            String name = node.getNodeName();
            String str = node.getNodeValue();

            if (str.contains("[")) {
                for (String s : NamingUtils.parseArray(str)) {
                    res.add(new NamedValue(name, Value.of(s)));
                }
            } else {
                res.add(new NamedValue(name, Value.of(str)));
            }
        }

        List<Element> elements = getElements(element);
        for (Element elNode : elements) {
            if (!(elNode.getElementsByTagName("*").getLength() > 0 || elNode.hasAttributes())) {
                String name = elNode.getTagName();
                if (elNode.getTextContent().isEmpty()) {
                    res.add(new NamedValue(name, Value.of(Boolean.TRUE)));
                } else {
                    String str = elNode.getTextContent();
                    if (str.contains("[")) {
                        for (String s : NamingUtils.parseArray(str)) {
                            res.add(new NamedValue(name, Value.of(s)));
                        }
                    } else {
                        res.add(new NamedValue(name, Value.of(str)));
                    }
                }

            }
        }
        return res;

    }

    @Override
    public boolean acceptsFile(File file) {
        return file.toString().toLowerCase().endsWith(".xml");
    }


}

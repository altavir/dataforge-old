/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaNode;
import hep.dataforge.values.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static hep.dataforge.io.IOUtils.UTF8_CHARSET;

/**
 * A writer for XML represented Meta
 *
 * @author Alexander Nozik
 */
public class XMLMetaWriter implements MetaStreamWriter {

    Charset charset = UTF8_CHARSET;

    @Override
    public XMLMetaWriter withCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public void write(OutputStream stream, Meta meta) {
        try {

            Document doc = getXMLDocument(meta);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            //PENDING add constructor customization of writer?
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset.displayName());
//            transformer.setOutputProperty(OutputKeys.METHOD, "text");
//            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(stream));
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Document getXMLDocument(Meta meta) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element element = getXMLElement(meta, doc);
            doc.appendChild(element);
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String normalizeName(String str) {
        return str.replace("@", "_at_");
    }

    private Element getXMLElement(Meta meta, Document doc) {
        String elementName;
        if (meta.isAnonimous()) {
            elementName = MetaNode.DEFAULT_META_NAME;
        } else {
            elementName = meta.getName();
        }
        Element res = doc.createElement(normalizeName(elementName));


        meta.getValueNames().forEach(valueName -> {
            List<Value> valueList = meta.getValue(valueName).listValue();
            if (valueList.size() == 1) {
                res.setAttribute(normalizeName(valueName), valueList.get(0).stringValue());
            } else {
                String val = valueList
                        .stream()
                        .map(Value::stringValue)
                        .collect(Collectors.joining(", ", "[", "]"));
                res.setAttribute(normalizeName(valueName), val);
            }
        });

        meta.getNodeNames().forEach(nodeName -> {
            List<? extends Meta> elementList = meta.getMetaList(nodeName);
            if (elementList.size() == 1) {
                Element el = getXMLElement(elementList.get(0), doc);
                res.appendChild(el);
            } else {
                for (Meta element : elementList) {
                    Element el = getXMLElement(element, doc);
                    res.appendChild(el);
                }
            }

        });
        return res;
    }
}

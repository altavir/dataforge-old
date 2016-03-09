/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

/**
 * A writer for XML represented Meta
 * @author Alexander Nozik
 */
public class XMLMetaWriter implements MetaStreamWriter {

    
    @Override
    public void write(OutputStream stream, Meta meta, Charset charset) {
        try {
            if(charset == null){
                charset = Charset.forName("UTF-8");
            }
            
            Document doc = getXMLDocument(meta);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            
            //PENDING add constructor customization of writer?
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset.displayName());
//            transformer.setOutputProperty(OutputKeys.METHOD, "text");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(stream));
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Document getXMLDocument(Meta meta) {
        try {
            DocumentBuilderFactory factory = newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element element = getXMLElement(meta, doc);
            doc.appendChild(element);
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Element getXMLElement(Meta an, Document doc) {
        String elementName;
        if (an.isAnonimous()) {
            elementName = "meta";
        } else {
            elementName = an.getName();
        }
        Element res = doc.createElement(elementName);
        MetaBuilder builder = an.getBuilder();
//        Map<String, Item<Annotation>> elements = builder.getElementMap();
//        Map<String, Item<Value>> values = builder.getValueMap();

        for (String name : an.getValueNames()) {
            List<Value> valueList = an.getValue(name).listValue();
            if (valueList.size() == 1) {
                res.setAttribute(name, valueList.get(0).stringValue());
            } else {
                String val = valueList
                        .stream()
                        .<String>map((v) -> v.stringValue())
                        .collect(Collectors.joining(", ", "[", "]"));
                res.setAttribute(name, val);
            }
        }

        for (String name : an.getNodeNames()) {
            List<? extends Meta> elementList = an.getNodes(name);
            if (elementList.size() == 1) {
                Element el = getXMLElement(elementList.get(0), doc);
                res.appendChild(el);
            } else {
                for (Meta element : elementList) {
                    Element el = getXMLElement(element, doc);
                    res.appendChild(el);
                }
            }

        }
        return res;
    }
}

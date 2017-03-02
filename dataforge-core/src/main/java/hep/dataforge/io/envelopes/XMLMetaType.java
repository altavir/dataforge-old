/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.io.XMLMetaReader;
import hep.dataforge.io.XMLMetaWriter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class XMLMetaType implements MetaType {
    public static final String XML_META_TYPE = "XML";
    public static XMLMetaType instance = new XMLMetaType();
    public static Short[] XML_META_CODES = {0x584d, 0};//XM

    @Override
    public List<Short> getCodes() {
        return Arrays.asList(XML_META_CODES);
    }

    @Override
    public String getName() {
        return XML_META_TYPE;
    }

    @Override
    public MetaStreamReader getReader() {
        return new XMLMetaReader();
    }

    @Override
    public MetaStreamWriter getWriter() {
        return new XMLMetaWriter();
    }

    @Override
    public Predicate<String> fileNameFilter() {
        return str-> str.toLowerCase().endsWith(".xml");
    }

}

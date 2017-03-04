/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.io.envelopes.MetaType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class JSONMetaType implements MetaType {
    public static final JSONMetaType instance = new JSONMetaType();
    public static Short[] JSON_META_CODES = {0x4a53, 1};//JS

    @Override
    public List<Short> getCodes() {
        return Arrays.asList(JSON_META_CODES);
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public MetaStreamReader getReader() {
        return new JSONMetaReader();
    }

    @Override
    public MetaStreamWriter getWriter() {
        return new JSONMetaWriter();
    }

    @Override
    public Predicate<String> fileNameFilter() {
        return str -> str.toLowerCase().endsWith(".json");
    }

}

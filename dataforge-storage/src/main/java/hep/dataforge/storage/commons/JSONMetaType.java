/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.io.envelopes.MetaType;

import java.util.function.Predicate;

public class JSONMetaType implements MetaType {
    public static final JSONMetaType instance = new JSONMetaType();

    @Override
    public short getCode() {
        return 1;
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
        return str-> str.toLowerCase().endsWith(".json");
    }

}

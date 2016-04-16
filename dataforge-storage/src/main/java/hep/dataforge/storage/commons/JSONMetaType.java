/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.io.envelopes.MetaType;

public class JSONMetaType implements MetaType {

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

}

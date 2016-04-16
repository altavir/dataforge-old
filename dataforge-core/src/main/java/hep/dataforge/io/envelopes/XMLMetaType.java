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

public class XMLMetaType implements MetaType {

    @Override
    public short getCode() {
        return 0;
    }

    @Override
    public String getName() {
        return "XML";
    }

    @Override
    public MetaStreamReader getReader() {
        return new XMLMetaReader();
    }

    @Override
    public MetaStreamWriter getWriter() {
        return new XMLMetaWriter();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.io.XMLMetaWriter;

/**
 * A singleton library for Meta stream writers. By default contains XML writer
 * @author Alexander Nozik
 */
public class MetaWriterLibrary extends PropertyLib<MetaStreamWriter> {

    private static final MetaWriterLibrary instance = new MetaWriterLibrary();

    public static MetaWriterLibrary instance() {
        return instance;
    }

    @Override
    public MetaStreamWriter getDefault() {
        return new XMLMetaWriter();
    }

    private MetaWriterLibrary() {
        putComposite(0, "XML", new XMLMetaWriter());
    }
}

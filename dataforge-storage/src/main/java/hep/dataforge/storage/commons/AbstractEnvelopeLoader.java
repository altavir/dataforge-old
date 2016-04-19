/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.EnvelopeLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.loaders.AbstractLoader;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractEnvelopeLoader extends AbstractLoader implements EnvelopeLoader {

    public AbstractEnvelopeLoader(Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
    }

    public AbstractEnvelopeLoader(Storage storage, String name) {
        super(storage, name);
    }
    
    @Override
    public Envelope respond(Envelope message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getType() {
        return ENVELOPE_LOADER_TYPE;
    }

    
}

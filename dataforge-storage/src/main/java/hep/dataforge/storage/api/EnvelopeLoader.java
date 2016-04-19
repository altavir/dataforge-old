/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import java.util.Iterator;

/**
 * A segmented loader containing an ordered set of envelopes
 *
 * @author Alexander Nozik
 */
public interface EnvelopeLoader extends Loader, Iterable<Envelope> {

    public static final String ENVELOPE_LOADER_TYPE = "envelope";

    /**
     * Push new envelope to loader
     *
     * @param env
     * @throws StorageException
     */
    void push(Envelope env) throws StorageException;
}

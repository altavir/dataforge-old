/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.exceptions.NotConnectedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageManager;

/**
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
public class StorageConnection extends DeviceConnection implements Responder {

    private Storage storage;

    /**
     * Connection to predefined storage
     *
     * @param storage
     */
    public StorageConnection(Storage storage) {
        this.storage = storage;
    }

    /**
     * Connection to dynamic storage created on open
     */
    public StorageConnection() {
    }

    @Override
    public boolean isOpen() {
        return storage != null && storage.isOpen();
    }

    @Override
    public void open(Object obj) throws Exception {
        super.open(obj);
        StorageManager storageManager = getDevice().getContext().pluginManager().getOrLoad(StorageManager.class);
        if (storage == null) {
            if (getDevice().meta().hasMeta("storage")) {
                storage = storageManager.buildStorage(getDevice().meta().getMeta("storage"));
            } else {
                storage = storageManager.getDefaultStorage();
            }
        }
        storage.open();
    }

    @Override
    public Envelope respond(Envelope message) {
        if (isOpen()) {
            return storage.respond(message);
        } else {
            return new MessageFactory().errorResponseBase(message, new NotConnectedException(this)).build();
        }
    }

    @Override
    public void close() throws Exception {
        if (isOpen()) {
            storage.close();
        }
        super.close();
    }

    public Storage getStorage() {
        return storage;
    }

}

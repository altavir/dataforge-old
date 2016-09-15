/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.devices.Device;
import hep.dataforge.exceptions.NotConnectedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Responder;
import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageManager;

/**
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
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
    public void open(Device device) throws Exception {
        StorageManager storageManager = device.getContext().getPlugin(StorageManager.class);
        if (storage == null) {
            if (device.meta().hasNode("storage")) {
                storage = storageManager.buildStorage(device.meta().getNode("storage"));
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
    }

    public Storage getStorage() {
        return storage;
    }

}

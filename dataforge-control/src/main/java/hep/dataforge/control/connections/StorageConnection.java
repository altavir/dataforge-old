/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.content.AnonimousNotAlowed;
import hep.dataforge.control.devices.Device;
import hep.dataforge.exceptions.NotConnectedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Responder;
import hep.dataforge.meta.Meta;
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
    private final String name;

    public StorageConnection(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOpen() {
        return storage != null && storage.isOpen();
    }

    @Override
    public void open(Device device) throws Exception {
        //FIXME laminate using internal meta here
        if (!device.getContext().provides("storage")) {
            device.getContext().loadPlugin("storage");
        }
        if (storage == null) {
            if (device.meta().hasNode("storage")) {
                storage = device.getContext().provide("storage", StorageManager.class).buildStorage(device.meta().getNode("storage"));
            } else {
                storage = device.getContext().provide("storage", StorageManager.class).getDefaultStorage();
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

//    @Override
//    public Meta meta() {
//        return meta == null ? Meta.buildEmpty("connection") : meta;
//    }
//    @Override
//    public Context getContext() {
//        return context;
//    }
    @Override
    public String type() {
        return "storage";
    }

    @Override
    public Meta meta() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.exceptions.NotConnectedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Responder;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StoragePlugin;

/**
 *
 * @author Alexander Nozik
 */
public class StorageConnection implements Connection, Annotated, Encapsulated, Responder {

    private final Meta meta;
    private Storage storage;
    private final Context context;

    public StorageConnection(Context context, Meta meta) {
        this.context = context;
        this.meta = meta;
        if(! context.provides("storage")){
            context.loadPlugin("storage");
        }
    }

    @Override
    public boolean isOpen() {
        return storage != null && storage.isOpen();
    }

    @Override
    public void open() throws Exception {
        if (storage == null) {
            if(meta().hasNode("storage")){
                storage = context.provide("storage", StoragePlugin.class).buildStorage(meta.getNode("storage"));
            } else {
                storage = context.provide("storage", StoragePlugin.class).getDefaultStorage();
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

    @Override
    public Meta meta() {
        return meta == null ? Meta.buildEmpty("connection") : meta;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public String type() {
        return "storage";
    }

}

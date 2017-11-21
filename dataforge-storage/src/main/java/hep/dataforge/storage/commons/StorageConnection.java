/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.context.ContextAware;
import hep.dataforge.control.Connectible;
import hep.dataforge.control.Connection;
import hep.dataforge.control.ConnectionFactory;
import hep.dataforge.exceptions.NotConnectedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.storage.api.Storage;

/**
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
public class StorageConnection implements Connection, Responder, Metoid, ContextAware {

    private final Meta meta;
    private final Storage storage;

    /**
     * Connection to predefined storage
     *
     * @param storage
     */
    public StorageConnection(Storage storage) {
        this.storage = storage;
        this.meta = storage.getMeta();
    }

    /**
     * Create storage from context and meta
     */
    public StorageConnection(Context context, Meta meta) {
        this.meta = meta;
        StorageManager storageManager = context.getPluginManager().getOrLoad(StorageManager.class);
        this.storage = storageManager.buildStorage(meta);
    }

    @Override
    public boolean isOpen() {
        return storage.isOpen();
    }

    @Override
    public void open(Object obj) throws Exception {
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

    @Override
    public Context getContext() {
        return storage.getContext();
    }

    @Override
    public Meta getMeta() {
        return meta;
    }

    public static class Factory implements ConnectionFactory {

        @Override
        public String getType() {
            return "df.storage";
        }

        @Override
        public <T extends Connectible> Connection build(T obj, Context context, Meta meta) {
            if (obj instanceof Metoid) {
                return new StorageConnection(context,
                        new Laminate(((Metoid) obj).getMeta().getMetaOrEmpty("storage"), meta)
                );
            } else {
                return new StorageConnection(context, meta);
            }
        }
    }
}

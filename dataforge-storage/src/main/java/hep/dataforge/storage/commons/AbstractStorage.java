/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provides;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;

import java.util.*;

/**
 * Конфигурации загрузчиков хранятся в оперативной памяти. Те, что поставляются
 * вместе с сервером загружаются в конструкторе, остальные загружаются только на
 * время выполнения. Для того, чтобы сделать другой формат хранения, нужно
 * переопредлить методы {@code setupLoader} и {@code overrideLoader}
 *
 * @author Darksnake
 */
public abstract class AbstractStorage implements Storage {

    public static final String DEFAULT_EVENT_LOADER_NAME = "@log";
    protected final Map<String, Loader> loaders = new HashMap<>();
    protected final Map<String, Storage> shelves = new HashMap<>();
    private final String name;
    protected Storage parent;
    protected Meta storageConfig;

    protected AbstractStorage(Storage parent, String name, Meta annotation) {
        this.name = name;
        this.storageConfig = annotation;
        this.parent = parent;
    }

    public AbstractStorage(String name, Meta annotation) {
        this(null, name, annotation);
    }

    public AbstractStorage(String name) {
        this(null, name, Meta.buildEmpty("storage"));
    }

    /**
     * Initialize this storage.
     *
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Override
    public void open() throws StorageException {

    }

    /**
     * Refresh the state of storage
     *
     * @throws StorageException
     */
    @Override
    public void refresh() throws StorageException {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    /**
     * Close the storage
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {

    }

    /**
     * Create shelf with given name and inherited configuration. By default is
     * equivalent of {@code buildShelf(shelfName, null)}
     *
     * @param shelfName
     * @return
     * @throws StorageException
     */
    public Storage buildShelf(String shelfName) throws StorageException {
        return buildShelf(shelfName, null);
    }

    /**
     * update an annotation of loader using overriding annotation
     *
     * @param currentLoader
     * @param newAnnotation
     * @return
     * @throws StorageException
     */
    protected Loader overrideLoader(Loader currentLoader, Meta newAnnotation) throws StorageException {
        if (currentLoader.meta().equalsIgnoreName(newAnnotation)) {
            return currentLoader;
        } else {
            throw new StorageException("Can't update loader with new annotation");
        }
    }

    @Override
    public Meta meta() {
        if (storageConfig == null) {
            return Meta.buildEmpty("storage");
        } else {
            return storageConfig;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Provides(LOADER_TARGET)
    public Optional<Loader> optLoader(String name) {
        return Optional.ofNullable(loaders.get(name));
    }


    @Override
    @Provides(STORAGE_TARGET)
    public Optional<Storage> optShelf(String name) {
        Name shelfName = Name.of(name);
        if (shelfName.length() == 1) {
            return Optional.ofNullable(shelves.get(name));
        } else {
            return optShelf(shelfName.getFirst().toString()).flatMap(child -> child.optShelf(shelfName.cutFirst().toString()));
        }
    }

    /**
     * map of direct descendants
     *
     * @return
     * @throws StorageException
     */
    @Override
    public Collection<Loader> loaders() throws StorageException {
        return Collections.unmodifiableCollection(loaders.values());
    }

    @Override
    public Collection<Storage> shelves() throws StorageException {
        return Collections.unmodifiableCollection(shelves.values());
    }

    /**
     * @return the parent
     */
    @Override
    public Storage getParent() {
        return parent;
    }

    /**
     * Build a loader on a specific shelf
     *
     * @param shelf
     * @param loaderConfig
     * @return
     * @throws StorageException
     */
    public Loader buildLoader(String shelf, Meta loaderConfig) throws StorageException {
        return optShelf(shelf).orElseGet(() -> buildShelf(shelf)).buildLoader(loaderConfig);
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    /**
     * Read only storage produces only read only loaders
     *
     * @return
     */
    public boolean isReadOnly() {
        return meta().getBoolean("readOnly", false);
    }

    @Override
    public Context getContext() {
        return Global.instance();
    }

    @Override
    public EventLoader getDefaultEventLoader() throws StorageException {
        return (EventLoader) buildLoader(new MetaBuilder("loader")
                .putValue(Loader.LOADER_NAME_KEY, DEFAULT_EVENT_LOADER_NAME)
                .putValue(Loader.LOADER_TYPE_KEY, EventLoader.EVENT_LOADER_TYPE)
                .build());
    }

    @Override
    @NodeDef(name = "security", info = "Some information for  security manager")
    public Envelope respond(Envelope message) {
        Responder responder = getResponder(message);
        if (responder.equals(this)) {
            //TODO add security management here
            //TODO implement
            return new MessageFactory().errorResponseBase("", new UnsupportedOperationException("Not supported yet.")).build();
        } else {
            return responder.respond(message);
        }
    }

    public MessageValidator getValidator() {
        return StorageUtils.defaultMessageValidator(STORAGE_TARGET, getName());
    }

//    @Override
//    public boolean acceptEnvelope(Envelope envelope) {
//        if (envelope.meta().hasMeta(ENVELOPE_DESTINATION_NODE)) {
//            Meta target = envelope.meta().getMeta(ENVELOPE_DESTINATION_NODE);
//            String targetType = target.getString(TARGET_TYPE_KEY, STORAGE_TARGET_TYPE);
//            if (targetType.equals(STORAGE_TARGET_TYPE)) {
//                String targetName = target.getString(TARGET_NAME_KEY);
//                return targetName.endsWith(getName());
//            } else {
//                return false;
//            }
//        } else {
//            LoggerFactory.getLogger(getClass()).debug("Envelope does not have target. Acepting by default.");
//            return true;
//        }
//    }
//
//    @Override
//    public Meta destinationMeta() {
//        return new MetaBuilder(ENVELOPE_DESTINATION_NODE)
//                .putValue(TARGET_TYPE_KEY, STORAGE_TARGET_TYPE)
//                .putValue(TARGET_NAME_KEY, getName())
//                .build();
//    }

    @Override
    @ValueDef(name = "name", info = "The name of storage or loader.")
    @ValueDef(name = "type", allowed = "[loader,storage]", def = "loader", info = "The type of target.")
    @ValueDef(name = "allowCreate", type = "BOOLEAN", def = "true",
            info = "Allow to create new loader or storage if it is not found.")
    @NodeDef(name = "meta", info = "A meta for sotrage or loader creation. Only used if 'allowCreate' is true.")
    public Responder getResponder(Meta targetInfo) {
        String targetType = targetInfo.getString(TARGET_TYPE_KEY, LOADER_TARGET);
        String targetName = targetInfo.getString(TARGET_NAME_KEY, "");
        boolean allowCreate = targetInfo.getBoolean("allowCreate", true);
        Meta addMeta = targetInfo.getMeta("meta", Meta.empty());
        try {
            switch (targetType) {
                case STORAGE_TARGET:
                    if (targetName.isEmpty()) {
                        return this;
                    } else {
                        return optShelf(targetName).orElseGet(() -> {
                            if (allowCreate) {
                                //TODO add some path parsing cutting first segment if it is the same as this storage name
                                return buildShelf(targetName, addMeta);
                            } else {
                                throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
                            }
                        });
                    }
                case LOADER_TARGET:
                    return optLoader(targetName).orElseGet(() -> {
                        if (allowCreate) {
                            return buildLoader(addMeta);
                        } else {
                            throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
                        }
                    });
                default:
                    throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
            }
        } catch (StorageException ex) {
            throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
        }
    }

}

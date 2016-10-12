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
import hep.dataforge.context.GlobalContext;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import hep.dataforge.navigation.Path;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурации загрузчиков хранятся в оперативной памяти. Те, что поставляются
 * вместе с сервером загружаются в конструкторе, остальные загружаются только на
 * время выполнения. Для того, чтобы сделать другой формат хранения, нужно
 * переопредлить методы {@code setupLoader} и {@code overrideLoader}
 *
 * @author Darksnake
 */
public abstract class AbstractStorage extends AbstractProvider implements Storage {

    public static final String LOADER_TARGET_TYPE = "loader";
    public static final String STORAGE_TARGET_TYPE = "storage";

    public static final String DEFAULT_EVENT_LOADER_NAME = "@log";

    protected Storage parent;
    private final String name;
    protected Meta storageConfig;

    protected final Map<String, Loader> loaders = new HashMap<>();

    protected final Map<String, Storage> shelves = new HashMap<>();

    protected AbstractStorage(Storage parent, String name, Meta annotation) {
        this.name = name;
        this.storageConfig = annotation;
        this.parent = parent;
    }

    public AbstractStorage(String name, Meta annotation) {
        this(null, name, annotation);
    }

    public AbstractStorage(String name) {
        this(null, name,Meta.buildEmpty("storage") );
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
    public boolean provides(Path path) {
        if (path.hasTail()) {
            return super.provides(path);
        } else {
            return provides("loader", path.name());
        }
    }

    /**
     * If chainpath is presented than default target is "storage". For single
     * path the default target is "loader"
     *
     * @param path
     * @return
     */
    @Override
    public Object provide(Path path) {
        if (path.hasTail()) {
            return super.provide(path);
        } else {
            return provide("loader", path.name());
        }
    }

    /**
     * If chainpath is presented than default target is "storage". For single
     * path the default target is "loader"
     *
     * @param target
     * @param name
     * @return
     */
    @Override
    protected boolean provides(String target, Name name) {
        if (target.isEmpty() || "storage".equals(target)) {
            return hasShelf(name.toString());
        } else if ("loader".equals(target)) {
            return hasLoader(name.toString());
        } else {
            throw new TargetNotProvidedException();
        }
    }

    @Override
    protected Object provide(String target, Name name) {
        try {
            if (target.isEmpty() || "storage".equals(target)) {
                return getShelf(name.toString());
            } else if ("loader".equals(target)) {
                return getLoader(name.toString());
            } else {
                throw new TargetNotProvidedException();
            }
        } catch (StorageException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected String defaultTagrget() {
        return "storage";
    }

    @Override
    protected String defaultChainTarget() {
        return "loader";
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
    public boolean hasLoader(String name) {
        return loaders.containsKey(name);
//        if (loaders.containsKey(name)) {
//            return true;
//        } else {
//            Name loaderName = Name.of(name);
//            if (loaderName.length() == 1) {
//                return false;
//            }
//            try {
//                Name shelfName = loaderName.cutLast();
//                Storage childShelf = getShelf(shelfName.toString());
//                if (childShelf == null) {
//                    return false;
//                } else {
//                    return childShelf.hasLoader(shelfName.getLast().toString());
//                }
//            } catch (StorageException ex) {
//                LoggerFactory.getLogger(getClass()).error("Error while triyngto find shelf", ex);
//                return false;
//            }
//        }
    }

    @Override
    public Loader getLoader(String name) throws StorageException {
//        refresh();
        if (loaders.containsKey(name)) {
            return loaders.get(name);
        } else {
            return null;
        }
//            Name loaderName = Name.of(name);
//            if (loaderName.length() == 1) {
//                return null;
//            }
//            try {
//                Name shelfName = loaderName.cutLast();
//                Storage childShelf = getShelf(shelfName.toString());
//                if (childShelf == null) {
//                    return null;
//                } else {
//                    return childShelf.getLoader(shelfName.getLast().toString());
//                }
//            } catch (StorageException ex) {
//                LoggerFactory.getLogger(getClass()).error("Error while triyngto find shelf", ex);
//                return null;
//            }
//        }
    }

    @Override
    public boolean hasShelf(String name) {
        Name shelfName = Name.of(name);
        if (shelfName.length() == 1) {
            return shelves.containsKey(name);
        } else {
            Storage child;
            try {
                child = getShelf(shelfName.getFirst().toString());
            } catch (StorageException ex) {
                LoggerFactory.getLogger(getClass()).error("Error while triyngto find shelf", ex);
                return false;
            }
            if (child == null) {
                return false;
            } else {
                return child.hasShelf(shelfName.cutFirst().toString());
            }
        }
    }

    /**
     * Get child shelf using recursive calling. Returns null if shelf not found
     *
     * @param name
     * @return
     * @throws StorageException
     */
    @Override
    public Storage getShelf(String name) throws StorageException {
        Name shelfName = Name.of(name);
        if (shelfName.length() == 1) {
            return shelves.get(name);
        } else {
            Storage child = getShelf(shelfName.getFirst().toString());
            if (child == null) {
                return null;
            } else {
                return child.getShelf(shelfName.cutFirst().toString());
            }
        }
    }

    /**
     * map of direct descendants
     *
     * @return
     * @throws StorageException
     */
    @Override
    public Map<String, Loader> loaders() throws StorageException {
        return new HashMap<>(loaders);
        //return Collections.unmodifiableMap(loaders);
    }

    @Override
    public Map<String, Storage> shelves() throws StorageException {
        return new HashMap<>(shelves);
//        return Collections.unmodifiableMap(shelves);
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
        if (this.hasShelf(shelf)) {
            return this.getShelf(shelf).buildLoader(loaderConfig);
        } else {
            Storage shelfStorage = buildShelf(shelf);
            return shelfStorage.buildLoader(loaderConfig);
        }
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
        return GlobalContext.instance();
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
        return StorageUtils.defaultMessageValidator(STORAGE_TARGET_TYPE, getName());
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
        String targetType = targetInfo.getString(TARGET_TYPE_KEY, LOADER_TARGET_TYPE);
        String targetName = targetInfo.getString(TARGET_NAME_KEY);
        boolean allowCreate = targetInfo.getBoolean("allowCreate", true);
        Meta addMeta = targetInfo.getMeta("meta", null);
        try {
            if (targetType.equals(STORAGE_TARGET_TYPE)) {
                if (targetName.equals(getName())) {
                    return this;
                } else if (hasShelf(targetName)) {
                    return getShelf(targetName);
                } else if (allowCreate) {
                    //TODO add some path parsing cutting first segment if it is the same as this storage name
                    return buildShelf(targetName, addMeta);
                } else {
                    throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
                }
            } else if (hasLoader(targetName)) {
                //TODO check for meta equality
                return getLoader(targetName);
            } else if (allowCreate) {
                return buildLoader(addMeta);
            } else {
                throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
            }
        } catch (StorageException ex) {
            throw new EnvelopeTargetNotFoundException(targetType, targetName, targetInfo);
        }
    }

}

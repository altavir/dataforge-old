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
import hep.dataforge.control.ConnectionHelper;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.events.Event;
import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provides;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.utils.MetaHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;
import static hep.dataforge.storage.commons.StorageUtils.buildPath;
import static hep.dataforge.values.ValueType.BOOLEAN;

/**
 * Конфигурации загрузчиков хранятся в оперативной памяти. Те, что поставляются
 * вместе с сервером загружаются в конструкторе, остальные загружаются только на
 * время выполнения. Для того, чтобы сделать другой формат хранения, нужно
 * переопредлить методы {@code setupLoader} и {@code overrideLoader}
 *
 * @author Darksnake
 */
@ValueDef(name = "readOnly", type = {BOOLEAN}, info = "Define if push operations are allowed in this storage")
public abstract class AbstractStorage extends MetaHolder implements Storage {

    protected final Map<String, Loader> loaders = new HashMap<>();
    protected final Map<String, Storage> shelves = new HashMap<>();
    private final String name;
    private final Context context;
    private final Storage parent;
    private final ConnectionHelper connectionHelper;

    protected AbstractStorage(@NotNull Storage parent, String name, Meta meta) {
        super(new Laminate(meta, parent.getMeta()));
        this.name = name;
        this.parent = parent;
        context = parent.getContext();
        connectionHelper = new ConnectionHelper(this, context.getLogger());
    }

    protected AbstractStorage(Context context, Meta meta) {
        super(meta);
        this.name = meta.getString("name", "");
        this.context = context;
        this.parent = null;
        connectionHelper = new ConnectionHelper(this, context.getLogger());
    }

    @Override
    public ConnectionHelper getConnectionHelper() {
        return connectionHelper;
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
        getLogger().debug("Closing storage {}", getFullPath());
        for (Storage shelf : shelves()) {
            shelf.close();
        }
        for (Loader loader : loaders()) {
            loader.close();
        }
    }

    /**
     * Create shelf with given name and inherited configuration. By default is
     * equivalent of {@code buildShelf(shelfName, null)}
     *
     * @param shelfName
     * @return
     * @throws StorageException
     */
    public final Storage buildShelf(String shelfName) throws StorageException {
        return buildShelf(shelfName, Meta.empty());
    }

    @Override
    public final Loader buildLoader(String loaderName, Meta loaderConfiguration) throws StorageException {
        Name name = Name.of(loaderName);
        if (name.length() == 1) {
            Loader loader = createLoader(loaderName, loaderConfiguration);
            this.loaders.put(loaderName, loader);
            return loader;
        } else {
            //delegate building to child storage
            return buildPath(this, name.cutLast()).buildLoader(name.getLast().toString(), loaderConfiguration);
        }
    }

    /**
     * Create a child loader but do not add it to loader list
     *
     * @param name
     * @param loaderConfiguration
     * @return
     * @throws StorageException
     */
    protected abstract Loader createLoader(String name, Meta loaderConfiguration) throws StorageException;


    @Override
    public final Storage buildShelf(String shelfName, Meta shelfConfiguration) throws StorageException {
        Name name = Name.of(shelfName);
        if (name.length() == 1) {
            Storage shelf = createShelf(shelfName, shelfConfiguration);
            this.shelves.put(shelfName, shelf);
            return shelf;
        } else {
            //delegate building to child storage
            return buildPath(this, name.cutLast()).buildShelf(name.getLast().toString(), shelfConfiguration);
        }
    }

    /**
     * Create a direct child shelf but do not add it to shelf list
     *
     * @param name
     * @param shelfConfiguration
     * @return
     * @throws StorageException
     */
    protected abstract Storage createShelf(String name, Meta shelfConfiguration) throws StorageException;

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

    public boolean isRoot() {
        return getParent() == null;
    }

    @Override
    public Context getContext() {
        return context;
    }

//    @Override
//    public EventLoader getDefaultEventLoader() throws StorageException {
//        return (EventLoader) buildLoader(new MetaBuilder("loader")
//                .putValue(Loader.LOADER_NAME_KEY, DEFAULT_EVENT_LOADER_NAME)
//                .putValue(Loader.LOADER_TYPE_KEY, EventLoader.EVENT_LOADER_TYPE)
//                .builder());
//    }

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
//                .builder();
//    }

    @Override
    @ValueDef(name = "name", info = "The name of storage or loader.")
    @ValueDef(name = "type", allowed = "[loader,storage]", def = "loader", info = "The type of target.")
    @ValueDef(name = "allowCreate", type = {BOOLEAN}, def = "true",
            info = "Allow to create new loader or storage if it is not found.")
    @NodeDef(name = DEFAULT_META_NAME, info = "A meta for sotrage or loader creation. Only used if 'allowCreate' is true.")
    public Responder getResponder(Meta targetInfo) {
        String targetType = targetInfo.getString(TARGET_TYPE_KEY, LOADER_TARGET);
        String targetName = targetInfo.getString(TARGET_NAME_KEY, "");
        boolean allowCreate = targetInfo.getBoolean("allowCreate", true);
        Meta addMeta = targetInfo.getMeta(DEFAULT_META_NAME, Meta.empty());
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
                            return buildLoader(targetName, addMeta);
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

    /**
     * Notify all connections which can handle events
     * @param event
     */
    protected void dispatchEvent(Event event){
        forEachConnection(EventHandler.class, eventHandler -> eventHandler.pushEvent(event));
    }

}

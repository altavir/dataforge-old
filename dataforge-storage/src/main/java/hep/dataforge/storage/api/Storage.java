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
package hep.dataforge.storage.api;

import hep.dataforge.context.ContextAware;
import hep.dataforge.control.AutoConnectible;
import hep.dataforge.control.RoleDef;
import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.AlphanumComparator;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;

import static hep.dataforge.control.Connection.EVENT_HANDLER_ROLE;
import static hep.dataforge.control.Connection.LOGGER_ROLE;

/**
 * The general interface for storage facility. Storage has its own annotation
 * and must be named. Storage's main purpose is to provide loaders.
 * <p>
 * Storage can have any number of sub-storages (shelves), but chain path is
 * supported only via provider interface.
 * </p>
 *
 * @author Darksnake
 */
@AnonymousNotAlowed
@RoleDef(name = EVENT_HANDLER_ROLE, objectType = EventHandler.class, info = "Handle events produced by this storage")
@RoleDef(name = LOGGER_ROLE, objectType = Logger.class, unique = true, info = "The logger for this storage")
public interface Storage extends Metoid, Named, Provider, AutoCloseable, Responder, Dispatcher, ContextAware, AutoConnectible, Comparable<Named> {
    //TODO consider removing dispatcher to helper classes

    String LOADER_TARGET = "loader";
    String STORAGE_TARGET = "storage";

    /**
     * Initialize this storage.
     *
     * @throws hep.dataforge.exceptions.StorageException
     */
    void open() throws StorageException;

    boolean isOpen();

    /**
     * Refresh the state of storage
     *
     * @throws StorageException
     */
    void refresh() throws StorageException;

    /**
     * Close the storage
     *
     * @throws Exception
     */
    @Override
    void close() throws Exception;

    /**
     * Creates a new loader with given configuration. Throws an exception if loader already exists.
     * The returned loader is not necessary a direct child of this storage
     *
     * @param loaderName
     * @param loaderConfiguration
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    Loader buildLoader(String loaderName, Meta loaderConfiguration) throws StorageException;

    /**
     * Create new substorage (shelf) in this storage. The shelf name could be
     * composite {@code  path.name}
     *
     * @param shelfName
     * @param shelfConfiguration
     * @return
     * @throws StorageException
     */
    Storage buildShelf(String shelfName, Meta shelfConfiguration) throws StorageException;

    /**
     * A map of all loaders in this storage and their annotations
     *
     * @return
     * @throws StorageException
     */
    Collection<Loader> loaders() throws StorageException;

    /**
     * A list of all shelves in this storage
     *
     * @return
     * @throws StorageException
     */
    Collection<Storage> shelves() throws StorageException;

    @Override
    default String defaultTarget() {
        return STORAGE_TARGET;
    }

    @Override
    default String defaultChainTarget() {
        return LOADER_TARGET;
    }

    /**
     * Get the loader with given name if it is registered in this storage. Chain
     * path not allowed.
     *
     * @param name
     * @return
     */
    Optional<Loader> optLoader(String name);

    /**
     * Returns th shelf with given name. Chain path not allowed. Throws
     * StorageException if shelf does not exist or not accessible.
     *
     * @param name
     * @return
     * @throws StorageException
     */
    Optional<Storage> optShelf(String name);

    /**
     * Get superStorage of this storage. If null, than this storage is root
     *
     * @return
     */
    @Nullable
    Storage getParent();

    /**
     * Get validator for
     *
     * @return
     */
    MessageValidator getValidator();


    /**
     * Read only storage produces only read only loaders
     *
     * @return
     */
    default boolean isReadOnly() {
        return meta().optValue("readOnly")
                .map(Value::booleanValue)
                .orElseGet(() -> getParent() != null && getParent().isReadOnly());
    }

    /**
     * Get relative path of this storage to given root storage.
     * If root is not ancestor of this storage or null, return full absolute path.
     *
     * @return
     */
    default Name getFullName(@Nullable Storage root) {
        if (getParent() == root || getParent() == null) {
            return Name.ofSingle(getName());
        } else {
            return getParent().getFullName(root).append(getName());
        }
    }

    default Name getFullName() {
        return getFullName(null);
    }

    /**
     * Full
     * @return
     */
    default Laminate getLaminate() {
        if (getParent() == null) {
            return new Laminate(meta());
        } else {
            return getParent().getLaminate().withFirstLayer(meta());
        }
    }


    @Override
    default int compareTo(@NotNull Named o) {
        return AlphanumComparator.INSTANCE.compare(this.getName(), o.getName());
    }

    @Override
    default Logger getLogger() {
        return optConnection(LOGGER_ROLE,Logger.class).orElse(getContext().getLogger());
    }
}

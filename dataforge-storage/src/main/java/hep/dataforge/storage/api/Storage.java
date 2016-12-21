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

import hep.dataforge.context.Encapsulated;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;

import java.util.Map;

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
@AnonimousNotAlowed
public interface Storage extends Annotated, Named, Provider, AutoCloseable, Responder, Dispatcher, Encapsulated{

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
     *
     * If the loader with given annotation is registered, than it is returned.
     * If not, it is registered and then returned. If given annotation is
     * different from the one that is stored for loader with its name than
     * loader configuration is changed if it is possible. Otherwise exception is
     * thrown.
     *
     * @param loaderConfiguration
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    Loader buildLoader(Meta loaderConfiguration) throws StorageException;

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
    Map<String, Loader> loaders() throws StorageException;

    /**
     * A map of all shelfs in this storage and their annotations
     *
     * @return
     * @throws StorageException
     */
    Map<String, Storage> shelves() throws StorageException;

    /**
     * Check if the loader with given name is registered. Chain path not
     * allowed.
     *
     * @param name
     * @return
     */
    boolean hasLoader(String name);

    /**
     * Get the loader with given name if it is registered in this storage. Chain
     * path not allowed.
     *
     * @param name name of the loader or data set. In general is the name of
     * hierarchical root element for all data points
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    Loader getLoader(String name) throws StorageException;

    /**
     * Check if this storage has shelf with given name. Chain path not allowed.
     *
     * @param name
     * @return
     */
    boolean hasShelf(String name);

    /**
     * Returns th shelf with given name. Chain path not allowed. Throws
     * StorageException if shelf does not exist or not accessible.
     *
     * @param name
     * @return
     * @throws StorageException
     */
    Storage getShelf(String name) throws StorageException;

    /**
     * Get superStorage of this storage. If null, than this storage is root
     *
     * @return
     */
    Storage getParent();

    /**
     * Get the default event loader for this storage
     *
     * @return
     */
    EventLoader getDefaultEventLoader() throws StorageException;

    /**
     * Get validator for
     * @return
     */
    MessageValidator getValidator();

    /**
     * Get the full path of this storage relative to root using '.' as a
     * separator
     *
     * @return
     */
    default String getFullPath() {
        if (getParent() == null) {
            return "";
        } else {
            String parentPath = getParent().getFullPath();
            if (parentPath.isEmpty()) {
                return getName();
            } else {
                return parentPath + "." + getName();
            }
        }
    }
}

/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.storage.api

import hep.dataforge.context.ContextAware
import hep.dataforge.control.AutoConnectible
import hep.dataforge.control.Connection.EVENT_HANDLER_ROLE
import hep.dataforge.control.Connection.LOGGER_ROLE
import hep.dataforge.control.RoleDef
import hep.dataforge.control.RoleDefs
import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.messages.Dispatcher
import hep.dataforge.io.messages.Responder
import hep.dataforge.io.messages.Validator
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.names.AlphanumComparator
import hep.dataforge.names.AnonymousNotAlowed
import hep.dataforge.names.Name
import hep.dataforge.names.Named
import hep.dataforge.providers.Provider
import org.slf4j.Logger
import java.util.*

/**
 * The general interface for storage facility. Storage has its own annotation
 * and must be named. Storage's main purpose is to provide loaders.
 *
 *
 * Storage can have any number of sub-storages (shelves), but chain path is
 * supported only via provider interface.
 *
 *
 * @author Darksnake
 */
@AnonymousNotAlowed
@RoleDefs(
        RoleDef(name = EVENT_HANDLER_ROLE, objectType = EventHandler::class, info = "Handle events produced by this storage"),
        RoleDef(name = LOGGER_ROLE, objectType = Logger::class, unique = true, info = "The logger for this storage")
)
interface Storage : Metoid, Named, Provider, AutoCloseable, Responder, Dispatcher, ContextAware, AutoConnectible, Comparable<Named> {

    val isOpen: Boolean

    /**
     * Get superStorage of this storage. If null, than this storage is root
     *
     * @return
     */
    val parent: Storage?

    /**
     * Get validator for
     *
     * @return
     */
    val validator: Validator


    /**
     * Read only storage produces only read only loaders
     *
     * @return
     */
    val isReadOnly: Boolean
        get() = meta.optValue("readOnly")
                .map<Boolean> { it.booleanValue() }
                .orElseGet { parent != null && parent!!.isReadOnly }

    val fullName: Name
        get() = getFullName(null)

    /**
     * Full
     * @return
     */
    val laminate: Laminate
        get() = if (parent == null) {
            Laminate(meta)
        } else {
            parent!!.laminate.withFirstLayer(meta)
        }

    /**
     * Initialize this storage.
     *
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    fun open()

    /**
     * Refresh the state of storage
     *
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun refresh()

    /**
     * Close the storage
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun close()

    /**
     * Creates a new loader with given configuration. Throws an exception if loader already exists.
     * The returned loader is not necessary a direct child of this storage
     *
     * @param loaderName
     * @param loaderConfiguration
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    fun buildLoader(loaderName: String, loaderConfiguration: Meta): Loader

    /**
     * Create new substorage (shelf) in this storage. The shelf name could be
     * composite `path.name`
     *
     * @param shelfName
     * @param shelfConfiguration
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun buildShelf(shelfName: String, shelfConfiguration: Meta): Storage

    /**
     * A map of all loaders in this storage and their annotations
     *
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun loaders(): Collection<Loader>

    /**
     * A list of all shelves in this storage
     *
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun shelves(): Collection<Storage>

    override fun defaultTarget(): String {
        return STORAGE_TARGET
    }

    override fun defaultChainTarget(): String {
        return LOADER_TARGET
    }

    /**
     * Get the loader with given name if it is registered in this storage. Chain
     * path not allowed.
     *
     * @param name
     * @return
     */
    fun optLoader(name: String): Optional<Loader>

    /**
     * Returns th shelf with given name. Chain path not allowed. Throws
     * StorageException if shelf does not exist or not accessible.
     *
     * @param name
     * @return
     * @throws StorageException
     */
    fun optShelf(name: String): Optional<Storage>

    /**
     * Get relative path of this storage to given root storage.
     * If root is not ancestor of this storage or null, return full absolute path.
     *
     * @return
     */
    fun getFullName(root: Storage?): Name {
        return if (parent === root || parent == null) {
            Name.ofSingle(name)
        } else {
            parent!!.getFullName(root).append(name)
        }
    }


    override fun compareTo(o: Named): Int {
        return AlphanumComparator.INSTANCE.compare(this.name, o.name)
    }

    override fun getLogger(): Logger {
        return optConnection(LOGGER_ROLE, Logger::class.java).orElse(context.logger)
    }

    companion object {
        //TODO consider removing dispatcher to helper classes

        const val LOADER_TARGET = "loader"
        const val STORAGE_TARGET = "storage"
    }
}

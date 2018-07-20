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

package hep.dataforge.storage


import hep.dataforge.Named
import hep.dataforge.connections.AutoConnectible
import hep.dataforge.connections.Connection
import hep.dataforge.connections.RoleDef
import hep.dataforge.connections.RoleDefs
import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.events.EventHandler
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.names.Name
import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.storage.StorageElement.Companion.STORAGE_TARGET
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.Logger
import java.util.*
import kotlin.reflect.KClass

/**
 * Generic storage element
 */
@RoleDefs(
        RoleDef(name = Connection.EVENT_HANDLER_ROLE, objectType = EventHandler::class, info = "Handle events produced by this storage"),
        RoleDef(name = Connection.LOGGER_ROLE, objectType = Logger::class, unique = true, info = "The logger for this storage")
)
interface StorageElement : Named, Metoid, Provider, ContextAware, AutoConnectible, AutoCloseable {
    /**
     * Parent of this storage element if present
     */
    val parent: StorageElement?

    /**
     * Full name relative to root storage
     */
    @JvmDefault
    val fullName: Name
        get() = parent?.fullName?.plus(name) ?: Name.ofSingle(name)

    companion object {
        const val STORAGE_TARGET = "storage"
    }
}

/**
 * Storage tree node
 */
interface Storage : StorageElement {

    /**
     * Top level children of this storage
     */
    val children: Map<String, StorageElement>

    /**
     * Names of direct children for provider
     */
    @get:ProvidesNames(STORAGE_TARGET)
    val childrenNames: Collection<String>
        get() = children.keys

    /**
     * Get storage element (name notation for recursive calls). Null if not present
     */
    @Provides(STORAGE_TARGET)
    operator fun get(name: String): StorageElement? {
        return get(Name.of(name))
    }

    /**
     * Resolve storage element by its fully qualified name
     */
    @JvmDefault
    operator fun get(name: Name): StorageElement? {
        return if (name.length == 1) {
            children[name.unescaped]
        } else {
            (get(name.first) as Storage?)?.get(name.cutFirst())
        }
    }

    @JvmDefault
    override fun getDefaultTarget(): String = STORAGE_TARGET

    /**
     * By default closes all children on close
     */
    @JvmDefault
    override fun close() {
        children.values.forEach { it.close() }
    }
}

/**
 * Mutable version of the storage
 */
interface MutableStorage : Storage {
    /**
     * Create a new element of the storage. If element with this name already exists, checks meta and either does nothing or throws exception.
     */
    suspend fun createElement(meta: Meta): StorageElement
}

/**
 * Leaf element of the storage tree.
 * @param T - the type of loader entry
 */
interface Loader<T: Any> : StorageElement, Iterable<T>{
    /**
     * The explicit type of the element
     */
    val type: KClass<T>
}

/**
 * Loader that could be appended after creation. Appending must be thread safe.
 */
interface AppendableLoader<T: Any> : Loader<T> {

    /**
     * Synchronously append loader and return when operation is complete
     */
    fun append(item: T)
}

/**
 * Loader which could be accessed by keys
 */
interface IndexedLoader<K: Comparable<K>, T: Any> : Loader<T> {
    /**
     * List of available loader keys. Duplicate keys are not allowed
     */
    val keys: NavigableSet<K>

    /**
     * Get loader element by its key. If key is not present, return null
     */
    @JvmDefault
    operator fun get(key: K): T? {
        return runBlocking {
            getInFuture(key)?.await()
        }
    }

    /**
     * Deferred element retrieval
     */
    fun getInFuture(key: K): Deferred<T>?
}

/**
 * Mutable version of indexed loader. Set operation is thread safe
 */
interface MutableIndexedLoader<K: Comparable<K>, T: Any> : IndexedLoader<K, T> {

    @JvmDefault
    operator fun set(key: K, value: T) {
        runBlocking {
            setInFuture(key, value).await()
        }
    }

    /**
     * Deferred writing procedure
     */
    suspend fun setInFuture(key: K, value: T): Deferred<T>
}

interface StorageFactory : Named {

    fun createElement(context: Context, meta: Meta, parent: StorageElement? = null): StorageElement

    @JvmDefault
    fun createElement(parent: StorageElement, meta: Meta): StorageElement {
        return createElement(parent.context, meta, parent)
    }
}
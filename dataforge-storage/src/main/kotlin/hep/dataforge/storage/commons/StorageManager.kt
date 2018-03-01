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
package hep.dataforge.storage.commons

import hep.dataforge.context.*
import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.Storage.Companion.STORAGE_TARGET
import hep.dataforge.storage.api.StorageType
import java.util.*
import java.util.stream.Stream

/**
 * @author darksnake
 */
@PluginDef(name = "storage", group = "hep.dataforge", info = "Basic DataForge storage plugin")
class StorageManager : BasicPlugin {

    /**
     * Storage registry
     */
    private val storages = HashMap<Meta, Storage>()

    /**
     * Return blank file storage in current working directory
     *
     * @return
     */
    val defaultStorage: Storage
        get() {
            try {
                return storages.values.stream().findFirst().orElseGet { buildStorage(Meta.empty()) }

            } catch (ex: StorageException) {
                throw RuntimeException("Can't initialize default storage", ex)
            }

        }


    constructor(meta: Meta) : super(meta) {
        if (meta.hasMeta("storage")) {
            meta.getMetaList("storage").forEach { this.buildStorage(it) }
        } else if (!meta.isEmpty) {
            buildStorage(meta)
        }
    }

    constructor() {}

    @Provides(STORAGE_TARGET)
    fun optStorage(name: String): Optional<Storage> {
        return storages.values.stream().filter { it -> it.name == name }.findAny()
    }

    @ProvidesNames(STORAGE_TARGET)
    fun storageNames(): Stream<String> {
        return storages.values.stream().map<String> { it.name }.distinct()
    }

    fun buildStorage(config: Meta): Storage {
        //FIXME fix duplicate names
        return storages.getOrPut(config) {
            val type = config.getString("type", DEFAULT_STORAGE_TYPE)
            val factory = getStorageFactory(type)
            if (factory.isPresent) {
                factory.get().build(context, config)
            } else {
                throw RuntimeException("Can't find Storage factory for type " + type)
            }
        }
    }

    override fun detach() {
        storages.values.forEach { shelf ->
            try {
                shelf.close()
            } catch (e: Exception) {
                logger.error("Failed to close storage", e)
            }
        }
        storages.clear()
        super.detach()
    }

    class Factory : PluginFactory() {

        override val type: Class<out Plugin>
            get() = StorageManager::class.java

        override fun build(meta: Meta): Plugin {
            return StorageManager(meta)
        }
    }

    companion object {

        /**
         * Get storage manager from given context. Attach new storage manager to
         * context if it is not provided
         *
         * @param context
         * @return
         */
        fun buildFrom(context: Context): StorageManager {
            return context[StorageManager::class.java]
        }


        private val loader = ServiceLoader.load(StorageType::class.java)
        private val DEFAULT_STORAGE_TYPE = "file"

        private fun getStorageFactory(type: String): Optional<StorageType> {
            for (st in loader) {
                if (st.type().equals(type, ignoreCase = true)) {
                    return Optional.of(st)
                }
            }
            return Optional.empty()
        }

        fun buildStorage(context: Context, meta: Meta): Storage {
            return context.load(StorageManager::class.java, Meta.empty()).buildStorage(meta)
        }
    }
}

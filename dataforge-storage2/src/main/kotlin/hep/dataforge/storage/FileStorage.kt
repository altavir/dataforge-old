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

import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.context.Context
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.EnvelopeReader
import hep.dataforge.io.envelopes.TaglessEnvelopeType
import hep.dataforge.meta.Meta
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.WatchService
import kotlin.streams.toList

interface FileStorageElement : StorageElement {
    val path: Path
}

interface FileStorageElementType<out T: FileStorageElement> {
    suspend fun create(parent: FileStorage, meta: Meta): T
    suspend fun read(parent: FileStorage, path: Path): T
}

open class FileStorage protected constructor(
        override val context: Context,
        override val meta: Meta,
        final override val path: Path,
        override val parent: StorageElement?,
        protected val typeMap: Map<String, FileStorageElementType<*>>
) : MutableStorage, FileStorageElement {

    private val _connectionHelper = ConnectionHelper(this)

    override fun getConnectionHelper(): ConnectionHelper = _connectionHelper

    private val _children = HashMap<String, FileStorageElement>()

    override val children: Map<String, StorageElement> = _children

    /**
     * Creating a watch service ore reusing one from parent
     */
    private val watchService: WatchService by lazy {
        (parent as? FileStorage)?.watchService ?: path.fileSystem.newWatchService()
    }

    private fun getFileName(file: Path): String {
        return file.fileName.toString()
    }

    override val name: String = getFileName(path)


    /**
     * Resolve element type from path if path represents an element, otherwise null.
     */
    protected open fun resolveType(path: Path): FileStorageElementType<*>? {
        return if (Files.isDirectory(path)) {
            Directory
        } else {
            null
        }
    }

    /**
     * Should be overridden in descendants to produce correct storage type
     */
    protected open fun buildShelf(path: Path, meta: Meta): FileStorage {
        return FileStorage(context, meta, path, parent, typeMap)
    }

    /**
     * Resolve element type from meta if it describes element, otherwise null.
     */
    protected open fun resolveType(meta: Meta, def: String = ""): FileStorageElementType<*>? {
        return typeMap[meta.getString("type", def)]
    }

    override suspend fun createElement(meta: Meta): StorageElement = resolveType(meta)?.create(this, meta)
            ?: throw RuntimeException("Can't create a storage element from $meta")

    /**
     * Update the current tree
     */
    protected suspend fun refresh() {
        synchronized(_children) {
            //update existing entries if needed
            Files.list(path).toList().forEach {
                val name = getFileName(it)
                if (!_children.contains(name)) {
                    resolveType(it)?.read(this, it)?.let {
                        _children[name] = it
                    }
                }
            }
            //Remove non-existent entries
            _children.filter { !Files.exists(it.value.path) }.entries.forEach {
                _children.remove(it.key, it.value)
            }

        }
    }

    object Directory : FileStorageElementType<FileStorageElement> {
        const val META_ENVELOPE_TYPE = "hep.dataforge.storage.meta"
        const val DIRECTORY_TYPE = "hep.dataforge.storage.directory"

        private fun resolveMetaFile(path: Path): Path {
            return Files.list(path)
                    .filter { it.fileName.toString() == "meta.df" || it.fileName.toString() == "meta" }
                    .findFirst().orElseGet { path.resolve("meta.df") }
        }

        private fun createMetaEnvelope(meta: Meta): Envelope {
            return EnvelopeBuilder().setMeta(meta).setEnvelopeType(META_ENVELOPE_TYPE).build()
        }

        override suspend fun create(parent: FileStorage, meta: Meta): FileStorageElement {
            val fileName = meta.getString("name")
            val path: Path = parent.path.resolve(fileName)
            Files.createDirectory(path)
            //writing meta to directory
            val metaFile = resolveMetaFile(path)
            Files.newOutputStream(metaFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                TaglessEnvelopeType.INSTANCE.writer.write(it, createMetaEnvelope(meta))
            }
            return parent.buildShelf(path, meta)
        }

        override suspend fun read(parent: FileStorage, path: Path): FileStorageElement {
            val metaFile = resolveMetaFile(path)
            val meta: Meta = if (Files.exists(metaFile)) {
                EnvelopeReader.readFile(metaFile).meta
            } else {
                Meta.empty()
            }
            return parent.buildShelf(path, meta)
        }


    }
}

open class EnvelopeStorage protected constructor(
        context: Context,
        meta: Meta,
        path: Path,
        parent: StorageElement?,
        typeMap: Map<String, FileStorageElementType<*>>
) : FileStorage(context, meta, path, parent, typeMap) {

    override fun resolveType(path: Path): FileStorageElementType<*>? {
        return if (Files.isDirectory(path)) {
            super.resolveType(path)
        } else {
            try {
                resolveType(EnvelopeReader.readFile(path).meta)
            } catch (ex: Exception) {
                logger.warn("Could not read file $path as envelope")
                null
            }
        }
    }

    override fun buildShelf(path: Path, meta: Meta): EnvelopeStorage {
        return EnvelopeStorage(context, meta, path, parent, typeMap)
    }
}

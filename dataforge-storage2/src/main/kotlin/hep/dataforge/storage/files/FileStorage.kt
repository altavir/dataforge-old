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

package hep.dataforge.storage.files

import hep.dataforge.Named
import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.context.Context
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.io.envelopes.EnvelopeType
import hep.dataforge.io.envelopes.TaglessEnvelopeType
import hep.dataforge.meta.Meta
import hep.dataforge.nullable
import hep.dataforge.storage.MutableStorage
import hep.dataforge.storage.StorageElement
import hep.dataforge.storage.StorageElementType
import hep.dataforge.storage.StorageManager
import kotlinx.coroutines.experimental.joinAll
import kotlinx.coroutines.experimental.launch
import java.net.URI
import java.nio.file.*
import kotlin.streams.asSequence
import kotlin.streams.toList

/**
 * An element of file storage with fixed path
 */
interface FileStorageElement : StorageElement {
    val path: Path
}

/**
 * The type of file storage element
 */
interface FileStorageElementType : StorageElementType, Named {
    /**
     * Create a new child for given parent. If child already exists, compare the meta. If same - return it, otherwise, throw exception
     */
    override suspend fun create(context: Context, meta: Meta, parent: StorageElement?): FileStorageElement

    /**
     * Read given path as [FileStorageElement] with given parent. Returns null if path does not belong to storage
     */
    suspend fun read(context: Context, path: Path, parent: StorageElement? = null): FileStorageElement?
}

class FileStorage(
        override val context: Context,
        override val name: String,
        override val meta: Meta,
        override val path: Path,
        override val parent: StorageElement? = null,
        val type: FileStorageElementType
) : MutableStorage, FileStorageElement {

//    /**
//     * The list of available children types.
//     * Take types from parent otherwise cache them from the context
//     */
//    val types: List<FileStorageElementType> = (parent as? FileStorage)?.types
//            ?: context.provideAll("fileStorageType", FileStorageElementType::class.java).toList()

    private val _connectionHelper by lazy { ConnectionHelper(this) }

    override fun getConnectionHelper(): ConnectionHelper = _connectionHelper

    private var isInitialized = false
    private val _children = HashMap<Path, StorageElement>()

    override suspend fun getChildren(): Collection<StorageElement>{
        if(!isInitialized){
            refresh()
        }
        return _children.values
    }

    /**
     * Creating a watch service or reusing one from parent
     */
    private val watchService: WatchService by lazy {
        (parent as? FileStorage)?.watchService ?: path.fileSystem.newWatchService()
    }

    //TODO actually watch for file change

    override suspend fun create(meta: Meta): StorageElement {
        val path = path.resolve(meta.getString("path"))
        return _children.getOrPut(path) {
            MutableStorage.createNewElement(this, meta)
        }
    }

    /**
     * Manually refresh storage state
     */
    suspend fun refresh() {
        //Remove non-existent entries
        _children.keys.filter { !Files.exists(it) }.forEach { _children.remove(it) }

        //update existing entries if needed
        Files.list(path).map { path ->
            launch {
                if (!_children.contains(path)) {
                    type.read(context, path, this@FileStorage)?.let { _children[path] = it }
                            ?: logger.debug("Could not resolve type for $path in $this")
                }
            }
        }.toList().joinAll()
        isInitialized = true
    }

    companion object {

        const val META_ENVELOPE_TYPE = "hep.dataforge.storage.meta"

        /**
         * Resolve meta for given path if it is available. If directory search for file called meta or meta.df inside
         */
        fun resolveMeta(path: Path, metaReader: (Path) -> Meta? = { EnvelopeType.infer(it)?.reader?.read(it)?.meta }): Meta? {
            return if (Files.isDirectory(path)) {
                Files.list(path).asSequence()
                        .find { it.fileName.toString() == "meta.df" || it.fileName.toString() == "meta" }
                        ?.let(metaReader)
            } else {
                metaReader(path)
            }
        }

        fun createMetaEnvelope(meta: Meta): Envelope {
            return EnvelopeBuilder().meta(meta).setEnvelopeType(META_ENVELOPE_TYPE).build()
        }

        fun getFileName(file: Path): String {
            return file.fileName.toString().substringBeforeLast(".")
        }
    }

    open class Directory : FileStorageElementType {
        override val name: String = "hep.dataforge.storage.directory"

        @ValueDefs(
                ValueDef(key = "path", info = "The relative path to the shelf inside parent storage or absolute path"),
                ValueDef(key = "name", info = "The name of the new storage. By default use last segment shelf name")
        )
        override suspend fun create(context: Context, meta: Meta, parent: StorageElement?): FileStorageElement {
            val shelf = meta.getString("path")
            val shelfPath = Paths.get(URI(shelf))
            val path: Path = ((parent as? FileStorageElement)?.path ?: context.dataDir).resolve(shelfPath)
            val name = meta.getString("name", path.fileName.toString())
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
            //writing meta to directory
            val metaFile = path.resolve("meta.df")
            Files.newOutputStream(metaFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                TaglessEnvelopeType.INSTANCE.writer.write(it, createMetaEnvelope(meta))
            }
            return FileStorage(context, name, meta, path, parent, this).also {
                if (parent == null) {
                    context.load<StorageManager>().register(it)
                }
            }
        }

        override suspend fun read(context: Context, path: Path, parent: StorageElement?): FileStorageElement? {
            val meta = resolveMeta(path)
            val name = meta?.optString("name").nullable ?: path.fileName.toString()
            val type = meta?.optString("type").nullable?.let { context.load<StorageManager>().optType(it) } as? FileStorageElementType
            return if (type == null || type is Directory) {
                // Read path as directory if type not found and path is directory
                if (Files.isDirectory(path)) {
                    FileStorage(context, name, meta ?: Meta.empty(), path, parent, this)
                } else {
                    //Ignore file if it is not directory and do not have path
                    null
                }
            } else {
                //Otherwise delegate to the type
                type.read(context, path, parent)
            }.also {
                if (it != null && parent == null) {
                    context.load<StorageManager>().register(it)
                }
            }
        }
    }
}

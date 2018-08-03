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
import hep.dataforge.io.envelopes.*
import hep.dataforge.meta.Meta
import hep.dataforge.nullable
import hep.dataforge.storage.MutableStorage
import hep.dataforge.storage.StorageElement
import kotlinx.coroutines.experimental.joinAll
import kotlinx.coroutines.experimental.launch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.WatchService
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
interface FileStorageElementType : Named {
    /**
     * Create a new child for given parent. If child already exists, compare the meta. If same - return it, otherwise, throw exception
     */
    suspend fun create(parent: FileStorage, meta: Meta): FileStorageElement

    /**
     * Read given path as [FileStorageElement] with given parent. Returns null if path does not belong to storage
     */
    suspend fun read(parent: FileStorage, path: Path): FileStorageElement?
}

class FileStorage protected constructor(
        override val context: Context,
        override val meta: Meta,
        override val path: Path,
        override val parent: StorageElement? = null,
        val type: FileStorageElementType = Directory
) : MutableStorage, FileStorageElement {

    /**
     * The list of available children types.
     * Take types from parent otherwise cache them from the context
     */
    val types: List<FileStorageElementType> = (parent as? FileStorage)?.types
            ?: context.provideAll("fileStorageType", FileStorageElementType::class.java).toList()

    private val _connectionHelper by lazy { ConnectionHelper(this) }

    override fun getConnectionHelper(): ConnectionHelper = _connectionHelper

    private val _children = HashMap<String, FileStorageElement>()

    override val children = _children.values

    /**
     * Creating a watch service ore reusing one from parent
     */
    private val watchService: WatchService by lazy {
        (parent as? FileStorage)?.watchService ?: path.fileSystem.newWatchService()
    }

    override val name: String = getFileName(path)

    override suspend fun create(meta: Meta): StorageElement =
            (meta.optString("type").nullable?.let { typeName -> types.find { it.name == typeName } } ?: type)
                    .create(this, meta)

    /**
     * Update the current tree
     */
    override suspend fun open() {
        synchronized(_children) {
            //Remove non-existent entries
            _children.filter { !Files.exists(it.value.path) }.entries.forEach {
                _children.remove(it.key, it.value)
            }

            //update existing entries if needed
            Files.list(path).map { it ->
                launch {
                    val name = getFileName(it)
                    if (!_children.contains(name)) {
                        type.read(this@FileStorage, it)?.let {
                            _children[name] = it
                        } ?: logger.debug("Could not resolve type for $it in $this")
                    }
                }
            }.toList().joinAll()
        }
    }

    companion object {

        const val META_ENVELOPE_TYPE = "hep.dataforge.storage.meta"

        /**
         * Resolve meta for given path if it is available. If directory search for file called meta or meta.df inside
         */
        fun resolveMeta(path: Path): Meta? {
            return if (Files.isDirectory(path)) {
                Files.list(path).asSequence()
                        .find { it.fileName.toString() == "meta.df" || it.fileName.toString() == "meta" }
                        ?.let { EnvelopeReader.readFile(it).meta }
            } else {
                EnvelopeType.infer(path)?.reader?.read(path)?.meta
            }
        }

        fun createMetaEnvelope(meta: Meta): Envelope {
            return EnvelopeBuilder().meta(meta).setEnvelopeType(META_ENVELOPE_TYPE).build()
        }

        private fun getFileName(file: Path): String {
            return file.fileName.toString()
        }
    }

    object Directory : FileStorageElementType {
        override val name: String = "hep.dataforge.storage.directory"

        override suspend fun create(parent: FileStorage, meta: Meta): FileStorageElement {
            val fileName = meta.getString("name")
            val path: Path = parent.path.resolve(fileName)
            Files.createDirectory(path)
            //writing meta to directory
            val metaFile = path.resolve("meta.df")
            Files.newOutputStream(metaFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                TaglessEnvelopeType.INSTANCE.writer.write(it, createMetaEnvelope(meta))
            }
            return FileStorage(parent.context, meta, path, parent, Directory)
        }

        override suspend fun read(parent: FileStorage, path: Path): FileStorageElement? {
            val meta = resolveMeta(path)
            val type = meta?.optString("type").nullable?.let { type -> parent.types.find { it.name == type } }
            return if(type == null|| type == Directory){
                // Read path as directory if type not found and path is directory
                if(Files.isDirectory(path)) {
                    FileStorage(parent.context, meta ?: Meta.empty(), path, parent, Directory)
                } else{
                    //Ignore file if it is not directory and do not have path
                    null
                }
            } else{
                //Otherwise delegate to the type
                type.read(parent,path)
            }
        }
    }
}

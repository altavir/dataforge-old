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
package hep.dataforge.storage.filestorage

import hep.dataforge.context.Context
import hep.dataforge.exceptions.StorageException
import hep.dataforge.io.envelopes.EnvelopeBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.*
import hep.dataforge.storage.api.Loader.Companion.LOADER_TYPE_KEY
import hep.dataforge.storage.commons.AbstractStorage
import hep.dataforge.storage.commons.StorageUtils
import hep.dataforge.storage.filestorage.FileStorageEnvelopeType.FILE_STORAGE_ENVELOPE_TYPE
import org.apache.commons.io.FilenameUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.ClosedWatchServiceException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchService

/**
 * Сервер данных на локальных текстовых файлах.
 *
 * @author Darksnake
 */
open class FileStorage : AbstractStorage {

    /**
     * @return the dataDir
     */
    val dataDir: Path
    private var monitor: WatchService? = null

    /**
     * Create a child storage
     *
     * @param parent
     * @param config
     * @param shelf
     * @throws StorageException
     */
    @Throws(StorageException::class)
    protected constructor(parent: FileStorage, config: Meta, shelf: String) : super(parent, shelf, config) {
        //replacing points with path separators we builder directory structure

        dataDir = parent.dataDir.resolve(shelf.replace(".", File.separator))
        startup()
    }

    /**
     * Create a root storage in directory
     *
     * @param meta
     * @throws StorageException
     */
    @Throws(StorageException::class)
    constructor(context: Context, meta: Meta, path: Path) : super(context, meta) {
        this.dataDir = path
    }

    private fun checkIfEnvelope(file: Path): Boolean {
        try {
            FileChannel.open(file, READ).use { channel ->
                val buffer = ByteBuffer.allocate(30)
                channel.read(buffer)
                val bytes = String(buffer.array())
                return bytes.startsWith("#~") || bytes.endsWith("!#\r\n")
            }
        } catch (e: IOException) {
            return false
        }

    }

    private fun startup() {
        if (!Files.exists(dataDir)) {
            if (isReadOnly) {
                throw StorageException("The directory for read only file storage does not exist")
            } else {
                try {
                    Files.createDirectories(dataDir)
                } catch (ex: IOException) {
                    throw RuntimeException("Failed to create FileStorage directory")
                }

            }
        }

        if (!Files.isDirectory(dataDir)) {
            throw StorageException("File Storage should be based on directory.")
        }

        //starting directory monitoring
        if (meta.getBoolean("monitor", false)) {
            startMonitor()
        }
    }

    //    private WatchService getWatchService() throws IOException {
    //        Storage parent = getParent();
    //        if (monitor != null) {
    //            return monitor;
    //        } else if (parent == null||  !(parent instanceof FileStorage)) {
    //            this.monitor = dataDir.getFileSystem().newWatchService();
    //            return monitor;
    //        } else {
    //            return ((FileStorage) parent).getWatchService();
    //        }
    //    }

    //FIXME not working properly. Should use single monitor from root storage
    private fun startMonitor() {
        try {
            if (monitor == null) {
                monitor = dataDir.fileSystem.newWatchService()
                dataDir.register(monitor, ENTRY_CREATE, ENTRY_DELETE)
                Thread {
                    try {
                        while (true) {
                            val key = monitor!!.take()
                            for (event in key.pollEvents()) {
                                val kind = event.kind()

                                if (kind === OVERFLOW) {
                                    continue
                                }

                                val file = event.context()
                                if (file is Path) {
                                    updateFile(file)
                                } else {
                                    logger.warn("Unknown event context type in file monitor: {}", file.javaClass)
                                }
                            }

                            if (!key.reset()) {
                                break
                            }
                        }
                    } catch (ex: ClosedWatchServiceException) {
                        logger.debug("Monitor thread stopped")
                    } catch (ex: Exception) {
                        logger.error("Monitor thread stopped due to unhandled exception", ex)
                    }
                }.start()
            }
        } catch (ex: Exception) {
            logger.error("Failed to start FileStorage monitor", ex)
        }

    }

    private fun stopMonitor() {
        logger.debug("Stopping monitor in storage {}", fullName)
        if (monitor != null) {
            try {
                monitor!!.close()
            } catch (ex: Exception) {
                logger.error("Failed to stop file monitor")
            } finally {
                monitor = null
            }
        }
    }

    protected fun buildDirectoryMeta(directory: Path): Meta {
        return Meta.empty()
        //        return new MetaBuilder("storage")
        //                .putValue("file.timeModified", Instant.ofEpochMilli(directory.getContent().getLastModifiedTime()))
        //                .builder();
    }

    /**
     * The method should be overridden in descendants to add new loader types
     *
     * @param file
     */
    protected fun updateFile(file: Path) {
        if (Files.isDirectory(file)) {
            try {
                val dirName = file.fileName.toString()
                val shelf = createShelf(buildDirectoryMeta(file), dirName)
                shelves.putIfAbsent(dirName, shelf)
                shelf.refresh()
            } catch (ex: StorageException) {
                LoggerFactory.getLogger(javaClass)
                        .error("Can't create a File storage from subdirectory {} at {}",
                                file.fileName, dataDir)
            }

        } else {
            try {
                if (checkIfEnvelope(file)) {
                    val loader = buildLoader(file)
                    loaders.putIfAbsent(loader.name, loader)
                }
            } catch (ex: Exception) {
                logger.warn("Can't create a loader from {}", file.fileName)
            }

        }
    }

    @Throws(Exception::class)
    private fun buildLoader(file: Path): Loader {
        FileEnvelope.open(file, isReadOnly).use { envelope ->
            return when (envelope.meta.getString(LOADER_TYPE_KEY, "")) {
                TableLoader.TABLE_LOADER_TYPE -> FileTableLoader(this, FilenameUtils.getBaseName(envelope.file.fileName.toString()), envelope.meta, envelope)
                EventLoader.EVENT_LOADER_TYPE -> FileEventLoader(this, FilenameUtils.getBaseName(envelope.file.fileName.toString()), envelope.meta, envelope)
                StateLoader.STATE_LOADER_TYPE -> FileStateLoader(this, FilenameUtils.getBaseName(envelope.file.fileName.toString()), envelope.meta, envelope)
                ObjectLoader.OBJECT_LOADER_TYPE -> FileObjectLoader<Serializable>(this, FilenameUtils.getBaseName(envelope.file.fileName.toString()), envelope.meta, envelope)
                else -> throw StorageException(
                        "The loader type with type " + envelope.meta.getString("type", "'undefined'") + " is not supported"
                )
            }
        }
    }

    @Throws(Exception::class)
    override fun close() {
        super.close()
        stopMonitor()
    }

    @Throws(StorageException::class)
    override fun createLoader(loaderName: String, loaderConfiguration: Meta): Loader {
        val type = StorageUtils.loaderType(loaderConfiguration)

        return if (optLoader(loaderName).isPresent) {
            overrideLoader(optLoader(loaderName).get(), loaderConfiguration)
        } else {
            buildLoaderByType(loaderName, loaderConfiguration, type)
        }
    }

    @Throws(StorageException::class)
    private fun buildLoaderByType(loaderName: String, loaderConfiguration: Meta, type: String): Loader {
        return when (type) {
            TableLoader.TABLE_LOADER_TYPE -> createNewFileLoader(loaderName, loaderConfiguration, ".points")
            StateLoader.STATE_LOADER_TYPE -> createNewFileLoader(loaderName, loaderConfiguration, ".state")
            ObjectLoader.OBJECT_LOADER_TYPE -> createNewFileLoader(loaderName, loaderConfiguration, ".binary")
            EventLoader.EVENT_LOADER_TYPE -> createNewFileLoader(loaderName, loaderConfiguration, ".event")
            else -> throw StorageException("Loader type not supported")
        }
    }

    @Throws(StorageException::class)
    private fun createNewFileLoader(loaderName: String, meta: Meta, extension: String): Loader {
        try {
            val dataFile = dataDir.resolve(loaderName + extension)
            if (!Files.exists(dataFile)) {
                Files.newOutputStream(dataFile, WRITE, CREATE_NEW).use { stream ->
                    val emptyEnvelope = EnvelopeBuilder()
                            .setDataType(FILE_STORAGE_ENVELOPE_TYPE)
                            .meta(meta)
                            .build()
                    FileStorageEnvelopeType.writer.write(stream, emptyEnvelope)
                }
            }
            refresh()

            return optLoader(loaderName).orElseThrow { StorageException("Loader could not be initialized from existing file") }
        } catch (ex: IOException) {
            throw StorageException(ex)
        }

    }

    @Throws(StorageException::class)
    public override fun createShelf(shelfConfiguration: Meta, shelfName: String): FileStorage {
        return FileStorage(this, shelfConfiguration, shelfName)
    }

    @Throws(StorageException::class)
    override fun refresh() {
        try {
            this.shelves.clear()
            this.loaders.clear()

            Files.list(dataDir).forEach{ this.updateFile(it) }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    companion object {

        fun entryName(path: Path): String {
            return FilenameUtils.getBaseName(path.fileName.toString())
        }
    }
}

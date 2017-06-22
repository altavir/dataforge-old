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
package hep.dataforge.storage.filestorage;

import ch.qos.logback.classic.Level;
import hep.dataforge.context.Context;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.*;
import hep.dataforge.storage.commons.AbstractStorage;
import hep.dataforge.storage.commons.StorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

import static hep.dataforge.storage.filestorage.FileStorageEnvelopeType.FILE_STORAGE_ENVELOPE_TYPE;
import static hep.dataforge.values.ValueType.BOOLEAN;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Сервер данных на локальных текстовых файлах.
 *
 * @author Darksnake
 */
@ValueDef(name = "path", info = "Path to storage root")
@ValueDef(name = "monitor", type = {BOOLEAN}, def = "false",
        info = "Enable file system monitoring for synchronous acess to single storage from different instances")
@ValueDef(name = "type", def = "file", info = "The type of the storage")
public class FileStorage extends AbstractStorage {

    static {
        //set up slf4j bridge and logging level
        Logger logger = LoggerFactory.getLogger("org.apache.commons.vfs2");
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(Level.WARN);
        }
    }

    private final Path dataDir;
    private WatchService monitor;
//    private Thread storageMonitorThread;

    /**
     * Create a root storage in directory
     *
     * @param config
     * @throws StorageException
     */
    public FileStorage(Context context, Meta config) throws StorageException {
        super(context, config);
        this.dataDir = Paths.get(config.getString("path", "."));
        startup();
    }

    /**
     * Create a child storage
     *
     * @param parent
     * @param dirName
     * @param config
     * @throws StorageException
     */
    protected FileStorage(FileStorage parent, String dirName, Meta config) throws StorageException {
        super(parent, dirName, config);
        //replacing points with path separators we build directory structure
        dataDir = Paths.get(dirName.replace(".", File.separator));
        startup();
    }

    private boolean checkIfEnvelope(Path file) {
        try (SeekableByteChannel channel = FileChannel.open(file, READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(30);
            channel.read(buffer);
            String bytes = new String(buffer.array());
            return bytes.startsWith("#~") || bytes.endsWith("!#\r\n");
        } catch (IOException e) {
            return false;
        }
    }

    private void startup() {
        if (!Files.exists(dataDir)) {
            if (isReadOnly()) {
                throw new StorageException("The directory for read only file storage does not exist");
            } else {
                try {
                    Files.createDirectories(dataDir);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to create FileStorage directory");
                }
            }
        }

        if (!Files.isDirectory(dataDir)) {
            throw new StorageException("File Storage should be based on directory.");
        }

        //starting directory monitoring
        if (meta().getBoolean("monitor", false)) {
            startMonitor();
        }
    }

    private void startMonitor() {
        try {
            if (monitor == null) {
                monitor = dataDir.getFileSystem().newWatchService();
                dataDir.register(monitor, ENTRY_CREATE, ENTRY_DELETE);
                new Thread(() -> {
                    try {
                        while (true) {
                            WatchKey key = monitor.take();
                            for (WatchEvent<?> event : key.pollEvents()) {
                                WatchEvent.Kind<?> kind = event.kind();

                                if (kind == OVERFLOW) {
                                    continue;
                                }

                                Object file = event.context();
                                if (file instanceof Path) {
                                    updateFile((Path) file);
                                } else {
                                    getLogger().warn("Unknown event context type in file monitor: {}", file.getClass());
                                }
                            }

                            if (!key.reset()) {
                                break;
                            }
                        }
                    } catch (ClosedWatchServiceException ex) {
                        getLogger().debug("Monitor thread stopped");
                    } catch (Exception ex) {
                        getLogger().error("Monitor thread stopped due to unhandled exception", ex);
                    }
                }).start();
            }
        } catch (Exception ex) {
            getLogger().error("Failed to start FileStorage monitor", ex);
        }
    }

    private void stopMonitor() {
        getLogger().debug("Stopping monitor in storage {}", getFullPath());
        if (monitor != null) {
            try {
                monitor.close();
            } catch (Exception ex) {
                getLogger().error("Failed to stop file monitor");
            } finally {
                monitor = null;
            }
        }
    }

    protected synchronized void updateDirectoryLoaders() {
        try {
            this.shelves.clear();
            this.loaders.clear();

            Files.list(dataDir).forEach(this::updateFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Meta buildDirectoryMeta(Path directory) {
        return Meta.empty();
//        return new MetaBuilder("storage")
//                .putValue("file.timeModified", Instant.ofEpochMilli(directory.getContent().getLastModifiedTime()))
//                .build();
    }

    /**
     * The method should be overridden in descendants to add new loader types
     *
     * @param file
     */
    protected void updateFile(Path file) {
        if (Files.isDirectory(file)) {
            try {
                String dirName = file.getFileName().toString();
                FileStorage shelf = createShelf(dirName, buildDirectoryMeta(file));
                shelves.putIfAbsent(dirName, shelf);
                shelf.refresh();
            } catch (StorageException ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Can't create a File storage from subdirectory {} at {}",
                                file.getFileName(), getDataDir());
            }
        } else {
            try {
                if (checkIfEnvelope(file)) {
                    Loader loader = buildLoader(file);
                    loaders.putIfAbsent(loader.getName(), loader);
                }
            } catch (Exception ex) {
                getLogger()
                        .warn("Can't create a loader from {}", file.getFileName());
            }
        }
    }

    protected Loader buildLoader(Path file) throws Exception {
        try (FileEnvelope envelope = FileEnvelope.open(file, isReadOnly())) {
            switch (envelope.meta().getString("type", "")) {
                case PointLoader.POINT_LOADER_TYPE:
                    return FilePointLoader.fromEnvelope(this, envelope);
                case EventLoader.EVENT_LOADER_TYPE:
                    return FileEventLoader.fromEnvelope(this, envelope);
                case StateLoader.STATE_LOADER_TYPE:
                    return FileStateLoader.fromEnvelope(this, envelope);
                case ObjectLoader.OBJECT_LOADER_TYPE:
                    return FileObjectLoader.fromEnvelope(this, envelope);
                default:
                    throw new StorageException(
                            "The loader type with type " + envelope.meta().getString("type", "") + " is not supported"
                    );
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        stopMonitor();
    }

    @Override
    protected Loader createLoader(String loaderName, Meta loaderConfiguration) throws StorageException {
        String type = StorageUtils.loaderType(loaderConfiguration);

        if (optLoader(loaderName).isPresent()) {
            return overrideLoader(optLoader(loaderName).get(), loaderConfiguration);
        } else {
            return buildLoaderByType(loaderName, loaderConfiguration, type);
        }
    }

    protected Loader buildLoaderByType(String loaderName, Meta loaderConfiguration, String type) throws StorageException {
        switch (type) {
            case PointLoader.POINT_LOADER_TYPE:
                return createNewFileLoader(loaderName, loaderConfiguration, ".points");
            case StateLoader.STATE_LOADER_TYPE:
                return createNewFileLoader(loaderName, loaderConfiguration, ".state");
            case ObjectLoader.OBJECT_LOADER_TYPE:
                return createNewFileLoader(loaderName, loaderConfiguration, ".binary");
            case EventLoader.EVENT_LOADER_TYPE:
                return createNewFileLoader(loaderName, loaderConfiguration, ".event");
            default:
                throw new StorageException("Loader type not supported");
        }
    }

    protected Loader createNewFileLoader(String loaderName, Meta meta, String extension) throws StorageException {
        try {
            Path dataFile = dataDir.resolve(loaderName + extension);
            if (!Files.exists(dataFile)) {
                try (OutputStream stream = Files.newOutputStream(dataFile, WRITE, CREATE_NEW)) {
                    Envelope emptyEnvelope = new EnvelopeBuilder()
                            .setContentType(FILE_STORAGE_ENVELOPE_TYPE)
                            .setMeta(meta)
                            .build();
                    new FileStorageEnvelopeType().getWriter().write(stream, emptyEnvelope);
                }
            }
            refresh();

            return optLoader(loaderName).orElseThrow(() -> new StorageException("Loader could not be initialized from existing file"));
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public FileStorage createShelf(String path, Meta an) throws StorageException {
        return new FileStorage(this, path, an);
    }

    @Override
    public void refresh() throws StorageException {
        updateDirectoryLoaders();
    }

    /**
     * @return the dataDir
     */
    public Path getDataDir() {
        return dataDir;
    }
}

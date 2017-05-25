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
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.*;
import hep.dataforge.storage.commons.AbstractStorage;
import hep.dataforge.storage.commons.StorageUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import static hep.dataforge.storage.filestorage.FileStorageEnvelopeType.FILE_STORAGE_ENVELOPE_TYPE;
import static org.apache.commons.vfs2.FileType.FOLDER;

/**
 * Сервер данных на локальных текстовых файлах.
 *
 * @author Darksnake
 */
@ValueDef(name = "path", info = "Path to storage root")
@ValueDef(name = "monitor", type = "BOOLEAN", def = "false",
        info = "Enable file system monitoring for synchronous acess to single storage from different instances")
@ValueDef(name = "type", def = "file", info = "The type of the storage")
public class FileStorage extends AbstractStorage implements FileListener {

    static {
        //set up slf4j bridge and logging level
        Logger logger = LoggerFactory.getLogger("org.apache.commons.vfs2");
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(Level.WARN);
        }
    }

    private final FileObject dataDir;
    private DefaultFileMonitor monitor;

    /**
     * Create a root storage in directory
     *
     * @param config
     * @throws StorageException
     */
    public FileStorage(Context context, Meta config) throws StorageException {
        super(context, config);
        try {
            this.dataDir = VFSUtils.getFile(config.getString("path", "."));
            startup();
        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
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
        try {
            //replacing points with path separators we build directory structure
            dataDir = parent.dataDir.resolveFile(dirName.replace(".", File.separator));
            startup();
        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
    }

    private boolean checkIfEnvelope(FileObject file) {
        try {
            return file.getContent().getRandomAccessContent(RandomAccessMode.READ).readByte() == '#';
        } catch (IOException e) {
            return false;
        }
    }

    private void startup() throws FileSystemException, StorageException {
        if (!dataDir.exists()) {
            if (isReadOnly()) {
                throw new StorageException("The directory for read only file storage does not exist");
            } else {
                dataDir.createFolder();
            }
        }

        if (!dataDir.exists() || dataDir.getType() != FOLDER) {
            throw new StorageException("File Storage should be based on directory.");
        }

        //starting directory monitoring
        if (meta().getBoolean("monitor", false)) {
            startMonitor();
        }
    }

    private void startMonitor() {
        if (monitor == null) {
            monitor = new DefaultFileMonitor(this);
            monitor.setRecursive(false);
            monitor.addFile(dataDir);
            monitor.start();
        }
    }

    private void stopMonitor() {
        getContext().getLogger().debug("Stopping monitor in storage {}", getFullPath());
        if (monitor != null) {
            monitor.stop();
            monitor = null;
        }
    }

    protected synchronized void updateDirectoryLoaders() {
        try {
            this.shelves.clear();
            this.loaders.clear();

            for (FileObject file : getDataDir().getChildren()) {
                updateFile(file);
            }
        } catch (FileSystemException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Meta buildDirectoryMeta(FileObject directory) throws FileSystemException {
        return new MetaBuilder("storage")
                .putValue("file.timeModified", Instant.ofEpochMilli(directory.getContent().getLastModifiedTime()))
                .build();
    }

    /**
     * The method should be overridden in descendants to add new loader types
     *
     * @param file
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    protected void updateFile(FileObject file) throws FileSystemException {
        if (file.getType() == FOLDER) {
            try {
                String dirName = file.getName().getBaseName();
                FileStorage shelf = createShelf(dirName, buildDirectoryMeta(file));
                shelves.putIfAbsent(dirName, shelf);
                shelf.refresh();
            } catch (StorageException ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Can't create a File storage from subdirectory {} at {}",
                                file.getName(), getDataDir().getName().getPath());
            }
        } else {
            try {
                if (checkIfEnvelope(file)) {
                    Loader loader = buildLoader(file);
                    loaders.putIfAbsent(loader.getName(), loader);
                }
            } catch (Exception ex) {
                getContext().getLogger()
                        .warn("Can't create a loader from {}", file.getName());
            } finally {
                file.close();
            }
        }
    }

    protected Loader buildLoader(FileObject file) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), isReadOnly())) {
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
        dataDir.close();
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
//            String name = StorageUtils.loaderName(meta);
            FileObject dir = getDataDir();
            //Создаем путь к файлу, если его не существует
            if (!dir.exists()) {
                dir.createFolder();
            }

            FileObject dataFile = dir.resolveFile(loaderName + extension);
            if (!dataFile.exists()) {
                dataFile.createFile();
                try (OutputStream stream = dataFile.getContent().getOutputStream()) {
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
    public FileObject getDataDir() {
        return dataDir;
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        //evaluate only new files
        updateFile(event.getFile());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        //do nothing, we suppose that file could not be deleted in the runtime
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        //do nothing
    }

}

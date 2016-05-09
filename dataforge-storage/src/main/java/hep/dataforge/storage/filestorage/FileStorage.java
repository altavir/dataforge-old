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

import hep.dataforge.context.Context;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.Tag;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.ObjectLoader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.commons.AbstractStorage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import static hep.dataforge.storage.commons.EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.EVENT_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.OBJECT_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.POINT_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.STATE_LOADER_TYPE_CODE;
import hep.dataforge.storage.commons.StorageUtils;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import static org.apache.commons.vfs2.FileType.FOLDER;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.slf4j.LoggerFactory;

/**
 * Сервер данных на локальных текстовых файлах.
 *
 * @author Darksnake
 */
@ValueDef(name = "path", info = "Path to storage root")
@ValueDef(name = "monitor", type = "BOOLEAN", def = "false",
        info = "Enable file sistem monitoring for synchronous acess to single storage from different instances")
public class FileStorage extends AbstractStorage implements FileListener {

    public static final String LOADER_PATH_KEY = "path";
    public static final String STORAGE_CONFIGURATION_FILE = "storage.xml";

    /**
     * Create root File storage from annotation
     *
     * @param context
     * @param annotation
     * @return
     */
    public static FileStorage from(Context context, Meta annotation) {
        String repo = annotation.getString("path");
        try {
            return in(new File(repo), annotation);
        } catch (StorageException ex) {
            throw new RuntimeException("Can't create a storage from given configuration", ex);
        }
    }

    /**
     * Create root file storage in the given local directory. Annotation is
     * optional.
     *
     * @param directory
     * @param def
     * @return
     * @throws StorageException
     */
    public static FileStorage in(File directory, Meta def) throws StorageException {
        try {
            FileObject localRoot = VFSUtils.getLocalFile(directory);
            return in(localRoot, def);

        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
    }

    public static FileStorage in(FileObject remoteDir, Meta def) throws StorageException {
        FileStorage res = new FileStorage(remoteDir, def);
//        res.loadConfig(def);
        res.updateDirectoryLoaders();
        return res;
    }

    /**
     * Open existing storage in read only or read/write mode.
     *
     * @param remoteDir
     * @param readOnly
     * @return
     * @throws StorageException
     */
    public static FileStorage connect(FileObject remoteDir, boolean readOnly, boolean monitor) throws StorageException {
        try {
            if (!remoteDir.exists() || !remoteDir.getType().equals(FOLDER)) {
                throw new StorageException("Can't open storage. Target should be existing directory.");
            }
        } catch (FileSystemException ex) {
            throw new StorageException("Can't open storage.");
        }

        Meta meta = new MetaBuilder("storage")
                .setValue("type", "file")
                .setValue("readOnly", readOnly)
                .setValue("monitor", monitor);

        FileStorage res = new FileStorage(remoteDir, meta);
        res.refresh();
        return res;
    }

    public static FileStorage connect(File directory, boolean readOnly, boolean monitor) throws StorageException {
        try {
            FileObject localRoot = VFSUtils.getLocalFile(directory);
            return FileStorage.connect(localRoot, readOnly, monitor);

        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
    }

    private final FileObject dataDir;
    private DefaultFileMonitor monitor;

    /**
     * Create a root storage in directory
     *
     * @param dir
     * @param config
     * @throws StorageException
     */
    protected FileStorage(FileObject dir, Meta config) throws StorageException {
        super("root", config);
        this.dataDir = dir;
        try {
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

    private void startup() throws FileSystemException, StorageException {

        if (!isReadOnly()) {
            if (!dataDir.exists()) {
                dataDir.createFolder();
            }
        }
        if (!dataDir.exists() || dataDir.getType() != FOLDER) {
            throw new StorageException("File Storage should be based on directory.");
        }

        //starting direcotry monitoring
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
        if (monitor != null) {
            monitor.stop();
            monitor = null;
        }
    }

    public void toggleMonitor(boolean monitorActive) {
        if (monitorActive) {
            startMonitor();
        } else {
            stopMonitor();
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

    protected Meta buildDirectoryMeta(FileObject directory) throws FileSystemException{
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
                FileStorage shelf = new FileStorage(this, dirName, buildDirectoryMeta(file));
//                shelf.setReadOnly(isReadOnly());
                shelf.refresh();
                shelves.putIfAbsent(dirName, shelf);
            } catch (StorageException ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Can't create a File storage from subdirectory {} at {}",
                                file.getName(), getDataDir().getName().getPath());
            }
        } else {
            try {
                Tag stamp = readFileTag(file);
                if (stamp != null && stamp.type == DATAFORGE_STORAGE_ENVELOPE_CODE) {
                    Loader loader = buildLoader(file, stamp);
                    loaders.putIfAbsent(loader.getName(), loader);
                }
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Can't create a loader from file {} at {}", file.getName(), getDataDir().getName().getPath());
                throw new RuntimeException(ex);
            } finally {
                file.close();
            }
        }
    }

    public static Tag readFileTag(FileObject file) throws IOException {
        InputStream stream = file.getContent().getInputStream();
        byte[] bytes = new byte[Tag.TAG_LENGTH];
        stream.read(bytes);
        if (Tag.isValidTag(bytes)) {
            Tag tag = new Tag();
            tag.read(bytes);
            return tag;
        } else {
            return null;
        }
    }

    protected Loader buildLoader(FileObject file, Tag stamp) throws Exception {
        switch (stamp.dataType) {
            case POINT_LOADER_TYPE_CODE:
                return FilePointLoader.fromFile(this, file, isReadOnly());
            case STATE_LOADER_TYPE_CODE:
                return FileStateLoader.fromFile(this, file, isReadOnly());
            case EVENT_LOADER_TYPE_CODE:
                return FileEventLoader.fromFile(this, file, isReadOnly());
            case OBJECT_LOADER_TYPE_CODE:
                return FileObjectLoader.fromFile(this, file, isReadOnly());
            default:
                throw new StorageException("The loader type with code " + Integer.toHexString(stamp.dataType) + " is not supported");
        }

    }

    @Override
    public void close() throws Exception {
        stopMonitor();
        dataDir.close();
    }

    @Override
    public Loader buildLoader(Meta loaderConfiguration) throws StorageException {
        String type = StorageUtils.loaderType(loaderConfiguration);
        String name = StorageUtils.loaderName(loaderConfiguration);
        if (hasLoader(name)) {
            return overrideLoader(getLoader(name), loaderConfiguration);
        } else {
            return buildLoaderByType(loaderConfiguration, type);
        }
    }

    protected Loader buildLoaderByType(Meta loaderConfiguration, String type) throws StorageException {
        switch (type) {
            case PointLoader.POINT_LOADER_TYPE:
                return createNewFileLoader(loaderConfiguration, ".points", EnvelopeCodes.POINT_LOADER_TYPE_CODE);
            case StateLoader.STATE_LOADER_TYPE:
                return createNewFileLoader(loaderConfiguration, ".state", EnvelopeCodes.STATE_LOADER_TYPE_CODE);
            case ObjectLoader.OBJECT_LOADER_TYPE:
                return createNewFileLoader(loaderConfiguration, ".binary", EnvelopeCodes.OBJECT_LOADER_TYPE_CODE);
            case EventLoader.EVENT_LOADER_TYPE:
                return createNewFileLoader(loaderConfiguration, ".event", EnvelopeCodes.EVENT_LOADER_TYPE_CODE);
            default:
                throw new StorageException("Loader type not supported");
        }
    }

    protected Loader createNewFileLoader(Meta an, String extension, int dataType) throws StorageException {
        try {
            String name = StorageUtils.loaderName(an);
            FileObject dir = getDataDir();
            //Создаем путь к файлу, если его не существует
            if (!dir.exists()) {
                dir.createFolder();
            }

            FileObject dataFile = dir.resolveFile(name + extension);
            if (!dataFile.exists()) {
                dataFile.createFile();
                try (OutputStream stream = dataFile.getContent().getOutputStream()) {
                    Envelope emptyEnvelope = new EnvelopeBuilder()
                            .setEnvelopeType(DATAFORGE_STORAGE_ENVELOPE_CODE)
                            .setMeta(an)
                            .setMetaType("XML")
                            .setMetaEncoding("UTF-8")
                            .setInfiniteDataSize()
                            .setDataType(dataType)
                            .build();
                    DefaultEnvelopeWriter.instance.write(stream, emptyEnvelope, true);

                }
            }
            refresh();
            Loader loader = getLoader(name);
            if (loader == null) {
                throw new StorageException("Loader could not be initialized from existing file");
            }
            return loader;
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public FileStorage buildShelf(String path, Meta an) throws StorageException {

        //TODO add recusive shelves builders for composite paths
        //converting dataforge paths to file paths
        path = path.replace('.', File.separatorChar);
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
        //do nothing we suppose that file could not be deleted in the runtime
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        //do nothing
    }

}

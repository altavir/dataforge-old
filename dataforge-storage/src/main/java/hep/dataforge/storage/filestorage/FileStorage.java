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
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.XMLMetaReader;
import hep.dataforge.io.XMLMetaWriter;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.Tag;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.BinaryLoader;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.commons.AbstractStorage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import static hep.dataforge.storage.commons.EnvelopeCodes.BINARY_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.EVENT_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.POINT_LOADER_TYPE_CODE;
import static hep.dataforge.storage.commons.EnvelopeCodes.STATE_LOADER_TYPE_CODE;
import hep.dataforge.storage.commons.StorageUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        res.loadConfig(def);
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
    public static FileStorage open(FileObject remoteDir, boolean readOnly) throws StorageException {
        try {
            if (!remoteDir.exists() || !remoteDir.getType().equals(FOLDER)) {
                throw new StorageException("Can't open storage. Target should be existing directory.");
            }
        } catch (FileSystemException ex) {
            throw new StorageException("Can't open storage.");
        }
        FileStorage res = new FileStorage(remoteDir, null);
        res.setReadOnly(readOnly);
        res.refresh();
        return res;
    }

    public static FileStorage open(File directory, boolean readOnly) throws StorageException {
        try {
            FileObject localRoot = VFSUtils.getLocalFile(directory);
            return open(localRoot, readOnly);

        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
    }

    private final FileObject dataDir;
    DefaultFileMonitor monitor;

    /**
     * Create a root storage in directory
     *
     * @param dir
     * @param config
     * @throws StorageException
     */
    protected FileStorage(FileObject dir, Meta config) throws StorageException {
        super("root", config);
        try {
            this.dataDir = dir;

            if (!dataDir.exists()) {
                dataDir.createFolder();
            }

            if (dataDir.getType() != FOLDER) {
                throw new StorageException("File Storage should be based on directory.");
            }
            if (config != null) {
                saveConfig();
            }

            //starting direcotry monitoring
            monitor = new DefaultFileMonitor(this);
            monitor.setRecursive(false);
            monitor.addFile(dataDir);
            monitor.start();
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

            if (!dataDir.exists()) {
                dataDir.createFolder();
            }

            if (dataDir.getType() != FOLDER) {
                throw new StorageException("File Storage should be based on directory.");
            }

            if (config != null) {
                saveConfig();
            }

            monitor = new DefaultFileMonitor(this);
            monitor.setRecursive(false);
            monitor.addFile(dataDir);
            monitor.start();
        } catch (FileSystemException ex) {
            throw new StorageException(ex);
        }
    }
    
    private FileObject getCfgFile() throws FileSystemException {
        return getDataDir().resolveFile(STORAGE_CONFIGURATION_FILE);
    }

    private void loadConfig(Meta def) {
        try {
            FileObject cfgFile = getCfgFile();
            if (cfgFile.exists()) {
                this.storageConfig = MergeRule.getDefault()
                        .merge(new XMLMetaReader().read(cfgFile.getContent().getInputStream(), -1, null), def);
            }
        } catch (IOException | ParseException ex) {
            LoggerFactory.getLogger(getClass()).error("Can't load storage confgiuration from config.xml", ex);
        }
    }

    private void saveConfig() {
        try {
            FileObject cfg = getCfgFile();
            if (!cfg.exists() && meta() != null && !meta().isEmpty()) {
                new XMLMetaWriter().write(cfg.getContent().getOutputStream(), meta(), null);
            }
        } catch (FileSystemException ex) {
            LoggerFactory.getLogger(getClass()).error("Can't save storage annotation", ex);
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
                FileStorage shelf = new FileStorage(this, dirName, null);
                shelf.setReadOnly(isReadOnly());
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
                if (stamp != null && stamp.getType() == DATAFORGE_STORAGE_ENVELOPE_CODE) {
                    Loader loader = buildLoader(file, stamp);
                    loader.open();
                    loaders.putIfAbsent(loader.getName(), loader);
                }
            } catch (IOException | ParseException | StorageException ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Can't create a loader from file {} at {}", file.getName(), getDataDir().getName().getPath());
                throw new RuntimeException(ex);
            }
        }
    }

    public static Tag readFileTag(FileObject file) throws IOException {
        InputStream stream = file.getContent().getInputStream();
        byte[] bytes = new byte[Tag.TAG_LENGTH];
        stream.read(bytes);
        if (Tag.isValidTag(bytes)) {
            return new Tag(bytes);
        } else {
            return null;
        }
    }

    protected Loader buildLoader(FileObject file, Tag stamp) throws IOException, ParseException, StorageException {
        switch (stamp.getDataType()) {
            case POINT_LOADER_TYPE_CODE:
                return FilePointLoader.fromFile(this, file, isReadOnly());
            case STATE_LOADER_TYPE_CODE:
                return FileStateLoader.fromFile(this, file, isReadOnly());
            case EVENT_LOADER_TYPE_CODE:
                return FileEventLoader.fromFile(this, file, isReadOnly());
            case BINARY_LOADER_TYPE_CODE:

            default:
                throw new StorageException("The loader type with code " + Integer.toHexString(stamp.getDataType()) + " is not supported");
        }

    }

    @Override
    public void close() throws Exception {
        monitor.stop();
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
            case BinaryLoader.BINARY_LOADER_TYPE:
                return createNewFileLoader(loaderConfiguration, ".binary", EnvelopeCodes.BINARY_LOADER_TYPE_CODE);
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
            return getLoader(name);
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
        //TODO read annotation from storage.xml
        loadConfig(Meta.buildEmpty("fileStorage"));
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
        refresh();
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        refresh();
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        refresh();
    }

}

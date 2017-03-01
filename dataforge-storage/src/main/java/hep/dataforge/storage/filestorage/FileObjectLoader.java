/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.loaders.AbstractBinaryLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

import java.io.*;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A file loader to store Serializable java objects
 *
 * @author Alexander Nozik
 */
public class FileObjectLoader<T extends Serializable> extends AbstractBinaryLoader<T> {

    //FIXME concurrent access currently not available
    public static <T extends Serializable> FileObjectLoader<T> fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), readOnly)) {
            return fromEnvelope(storage, envelope);
        }
    }

    public static <T extends Serializable> FileObjectLoader<T> fromEnvelope(Storage storage, FileEnvelope envelope) throws Exception {
        if (FileStorageEnvelopeType.validate(envelope, OBJECT_LOADER_TYPE)) {
            FileObjectLoader res = new FileObjectLoader(storage,
                    FilenameUtils.getBaseName(envelope.getFile().getName().getBaseName()),
                    envelope.meta(),
                    envelope.getFile().getURL().toString());
            res.setReadOnly(envelope.isReadOnly());
            return res;
        } else {
            throw new StorageException("Is not a valid object loader file");
        }
    }

    private final String filePath;
    private final Map<String, T> dataMap = new HashMap<>();

    /**
     * An file used for pushing
     */
    private FileEnvelope file;

    public FileObjectLoader(Storage storage, String name, Meta annotation, String uri) {
        super(storage, name, annotation);
        this.filePath = uri;
    }

    public FileObjectLoader(Storage storage, String name, String uri) {
        super(storage, name);
        this.filePath = uri;
    }

    @Override
    public void open() throws Exception {
        if (this.meta == null) {
            this.meta = getFile().meta();
        }
        if (!isOpen()) {
            file = new FileEnvelope(filePath, isReadOnly());
        }
    }

    @Override
    public boolean isOpen() {
        return file != null;
    }

    @Override
    public void close() throws Exception {
        getFile().close();
        file = null;
        super.close();
    }

    private FileEnvelope getFile() throws Exception {
        if (file == null) {
            open();
        }
        return file;
    }

    @Override
    public boolean isEmpty() {
        try {
            return !getFile().hasData();
        } catch (Exception ex) {
            throw new RuntimeException("Can't access loader envelope", ex);
        }
    }

    public Map<String, T> getDataMap() throws StorageException {
        if (dataMap.isEmpty()) {
            dataMap.putAll(readDataMap());
        }
        return dataMap;
    }

    protected synchronized Map<String, T> readDataMap() throws StorageException {
        try (ObjectInputStream ois = new ObjectInputStream(getEnvelope().getDataStream())) {
            return (Map<String, T>) ois.readObject();
        } catch (Exception ex) {
            return new HashMap<>();
            //throw new StorageException(ex);
        }
    }

    protected synchronized void writeDataMap(Map<String, T> data) throws StorageException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(data);
            getEnvelope().clearData();
            getEnvelope().append(baos.toByteArray());
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    private FileEnvelope getEnvelope() throws StorageException {
        if (this.file == null) {
            this.file = buildEnvelope(false);
        }
        return this.file;
    }

    private FileEnvelope buildEnvelope(boolean readOnly) throws StorageException {
        try {
            return new FileEnvelope(filePath, readOnly);
        } catch (IOException | ParseException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public Collection<String> fragmentNames() {
        try {
            return getDataMap().keySet();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public T pull(String fragmentName) throws StorageException {
        return getDataMap().get(fragmentName);
    }

    @Override
    public void push(String fragmentName, T data) throws StorageException {
        getDataMap().put(fragmentName, data);
        writeDataMap(dataMap);
    }

}

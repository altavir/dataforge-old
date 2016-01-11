/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.commons.MapIndex;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public abstract class FileMapIndex<T> extends MapIndex<T, Integer> implements Serializable, Encapsulated {

    private final transient Context context;
    private final Supplier<FileEnvelope> envelopeProvider;
    private long lastIndexedPosition = -1;
    private long lastSavedPosition = -1;

    public FileMapIndex(Context context, Supplier<FileEnvelope> envelopeProvider) {
        this.context = context;
        this.envelopeProvider = envelopeProvider;
    }

    @Override
    protected synchronized void update() throws StorageException {
        try {
            if (map.isEmpty()) {
                loadIndex();
            }
            FileEnvelope env = getEnvelope();
            if (!isUpToDate(env)) {
                if (lastIndexedPosition >= 0) {
                    env.seek(lastIndexedPosition);
                } else {
                    env.resetPos();
                }
                while (!env.isEof()) {
                    long pos = env.readerPos();
                    String str = env.readLine();
                    T entry = readEntry(str);
                    Value indexValue = getIndexedValue(entry);
                    putToIndex(indexValue, (int) pos);
                }
                lastIndexedPosition = env.eofPos();
            }
            if (needsSave()) {
                saveIndex();
            }
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }
    
    protected abstract String indexFileName();

    protected abstract T readEntry(String str);
    
    protected boolean needsSave(){
        return lastIndexedPosition - lastSavedPosition >= 200;
    }

    @Override
    protected T transform(Integer key) {
        try {
            return readEntry(getEnvelope().readLine(key));
        } catch (IOException ex) {
            throw new RuntimeException("Can't read entry for key " + key, ex);
        }
    }

    private boolean isUpToDate(FileEnvelope env) throws IOException {
        return env.eofPos() == this.lastIndexedPosition;
    }

    @Override
    public void invalidate() throws StorageException {
        File indexFile = getIndexFile();
        if (indexFile.exists()) {
            indexFile.delete();
        }
        lastIndexedPosition = -1;
        super.invalidate();
    }

    private FileEnvelope getEnvelope(){
        return envelopeProvider.get();
    }

    private File getIndexFileDirectory() {
        return new File(context.io().getTmpDirectory(), "storage/fileindex");
    }

    private File getIndexFile() throws StorageException {
        FileEnvelope env = getEnvelope();
        return new File(getIndexFileDirectory(), indexFileName());
    }

    @Override
    public Context getContext() {
        return context;
    }

//    private void setContext(Context context) {
//        this.context = context;
//    }

    /**
     * Load index content from external file
     */
    private synchronized void loadIndex() throws StorageException {
        File indexFile = getIndexFile();
        if (indexFile.exists()) {
            LoggerFactory.getLogger(getClass()).info("Loading index from file...");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
                long position = ois.readLong();
                TreeMap<Value, List<Integer>> newMap = (TreeMap<Value, List<Integer>>) ois.readObject();
                if (position > 0 && position >= this.lastIndexedPosition && newMap != null) {
                    this.map = newMap;
                    this.lastIndexedPosition = position;
                }
            } catch (IOException | ClassNotFoundException ex) {
                LoggerFactory.getLogger(getClass()).error("Failed to read index file. Removing index file", ex);
                indexFile.delete();
            }
        } else {
            LoggerFactory.getLogger(getClass()).debug("Index file not found");
        }
    }

    private synchronized void saveIndex() throws StorageException {
        File indexFile = getIndexFile();
        try {
            LoggerFactory.getLogger(getClass()).info("Saving index to file...");
            if (!indexFile.exists()) {
                indexFile.createNewFile();
            }
            try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(indexFile))) {
                ous.writeLong(lastIndexedPosition);
                ous.writeObject(map);
            }
            lastSavedPosition = lastIndexedPosition;
        } catch (IOException ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to write index file. Removing index file.", ex);
            indexFile.delete();
        }
    }

}

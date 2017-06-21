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
import hep.dataforge.values.ValueUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * @author Alexander Nozik
 */
public abstract class FileMapIndex<T> extends MapIndex<T, Integer> implements Serializable, Encapsulated {

    private final transient Context context;
    private final Supplier<FileEnvelope> envelopeProvider;

    /**
     * The size of the data when last indexed
     */
    private int indexedSize = 0;

    /**
     * The size of the data when last saved
     */
    private long savedSize = -1;

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
                ByteBuffer buffer = env.getData().getBuffer(indexedSize);
                buffer.position(0);
                int linePos = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (buffer.hasRemaining()) {
                    byte next = buffer.get();
                    if (next == '\n') {
                        String line = new String (baos.toByteArray(), "UTF8");
                        String str = line.trim();
                        if (!str.startsWith("#") && !str.isEmpty()) {
                            T entry = readEntry(str);
                            Value indexValue = getIndexedValue(entry);
                            putToIndex(indexValue, linePos);
                        }
                        //resetting collection
                        baos.reset();
                        linePos = buffer.position();
                    } else {
                        baos.write(next);
                    }

                }
                indexedSize = buffer.position();
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

    protected boolean needsSave() {
        return indexedSize - savedSize >= 200;
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
        return env.getData().size() == this.indexedSize;
    }

    @Override
    public void invalidate() throws StorageException {
        File indexFile = getIndexFile();
        if (indexFile.exists()) {
            indexFile.delete();
        }
        indexedSize = 0;
        super.invalidate();
    }

    private FileEnvelope getEnvelope() {
        return envelopeProvider.get();
    }

    private File getIndexFileDirectory() {
        return new File(context.io().getTmpDirectory(), "storage/fileindex");
    }

    private File getIndexFile() throws StorageException {
//        FileEnvelope env = getEnvelope();
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
                int position = (int) ois.readLong();
                TreeMap<Value, List<Integer>> newMap = new TreeMap<>(ValueUtils.VALUE_COMPARATPR);
                while (ois.available() > 0) {
                    Value val = ValueUtils.readValue(ois);
                    short num = ois.readShort();
                    List<Integer> integers = new ArrayList<>();
                    for (int i = 0; i < num; i++) {
                        integers.add(ois.readInt());
                    }
                    newMap.put(val, integers);
                }


                if (position > 0 && position >= this.indexedSize) {
                    this.map = newMap;
                    this.indexedSize = position;
                }
            } catch (IOException | ClassNotFoundException ex) {
                LoggerFactory.getLogger(getClass()).error("Failed to read index file. Removing index file", ex);
                indexFile.delete();
            }
        } else {
            LoggerFactory.getLogger(getClass()).debug("Index file not found");
        }
    }

    /**
     * Save index to default file
     *
     * @throws StorageException
     */
    private synchronized void saveIndex() throws StorageException {
        File indexFile = getIndexFile();
        try {
            LoggerFactory.getLogger(getClass()).info("Saving index to file...");
            if (!indexFile.exists()) {
                if (!indexFile.getParentFile().exists()) {
                    indexFile.getParentFile().mkdirs();
                }
                indexFile.createNewFile();
            }
            try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(indexFile))) {
                ous.writeLong(indexedSize);
                map.forEach((value, integers) -> {
                    try {
                        ValueUtils.writeValue(ous, value);
                        ous.writeShort(integers.size());
                        for (Integer i : integers) {
                            ous.writeInt(i);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            savedSize = indexedSize;
        } catch (IOException ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to write index file. Removing index file.", ex);
            if (indexFile.exists()) {
                indexFile.delete();
            }
        }
    }

}

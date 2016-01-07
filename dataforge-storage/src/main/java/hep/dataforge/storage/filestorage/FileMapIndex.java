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
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 *
 * @author Alexander Nozik
 */
public abstract class FileMapIndex<T> extends MapIndex<T, Integer> implements Serializable, Encapsulated {

    private transient Context context;
    private Supplier<FileEnvelope> envelopeProvider;
    private long lastIndexedPosition = -1;

    @Override
    protected synchronized void update() throws StorageException {
        try {
            FileEnvelope env = getEnvelope();
            if (!isUpToDate(env)) {
                if (lastIndexedPosition >= 0) {
                    env.seek(lastIndexedPosition);
                } else {
                    env.resetPos();
                }
                while(!env.isEof()){
                    long pos = env.readerPos();
                    String str = env.readLine();
                    T entry = readEntry(str);
                    Value indexValue = getIndexedValue(entry);
                    putToIndex(indexValue, (int) pos);
                }
                lastIndexedPosition = env.eofPos();
            }
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    protected abstract T readEntry(String str);

    @Override
    protected T transform(Integer key) {
        try {
            return readEntry(getEnvelope().readLine(key));
        } catch (StorageException |IOException ex) {
            throw new RuntimeException("Can't read entry for key "+key, ex);
        }
    }

    private boolean isUpToDate(FileEnvelope env) throws IOException {
        return env.eofPos() == this.lastIndexedPosition;
    }

    @Override
    public void invalidate() throws StorageException {
        File indexFile = getIndexFile(getEnvelope());
        if (indexFile.exists()) {
            indexFile.delete();
        }
        lastIndexedPosition = -1;
        super.invalidate();
    }

    private FileEnvelope getEnvelope() throws StorageException {
        return envelopeProvider.get();
    }

    private File getIndexFileDirectory() {
        return new File(context.io().getTmpDirectory(), "storage/fileindex");
    }

    private File getIndexFile(FileEnvelope env) {
        return new File(getIndexFileDirectory(), "index_" + env.getFilePath().hashCode());
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }
    
    private void loadIndex(){

    }
    
    private void saveIndex(){
        
    }

}

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

import hep.dataforge.events.Event;
import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import static hep.dataforge.storage.filestorage.FilePointLoader.isValidFilePointLoaderEnvelope;
import hep.dataforge.storage.loaders.AbstractEventLoader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.function.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;

/**
 *
 * @author darksnake
 */
public class FileEventLoader extends AbstractEventLoader {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final byte[] NEWLINE = {'\r', '\n'};

    public static FileEventLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file, readOnly)) {
            if (isValidFilePointLoaderEnvelope(envelope)) {
                FileEventLoader res = new FileEventLoader(file.getURL().toString(),
                        storage, FilenameUtils.getBaseName(file.getName().getBaseName()),
                        envelope.meta());
                res.setReadOnly(readOnly);
                return res;
            } else {
                throw new StorageException("Is not a valid point loader file");
            }
        }
    }

    public static boolean isValidFileEventLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.EVENT_LOADER_TYPE_CODE;
    }

    private final String filePath;
    private FileEnvelope file;
    private Predicate<Event> filter;

    public FileEventLoader(String filePath, Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
        this.filePath = filePath;
    }

    @Override
    public void open() throws Exception {
        if (!isOpen()) {
            super.open();
            FileObject fileObject = VFS.getManager().resolveFile(filePath);
            file = new FileEnvelope(fileObject, isReadOnly());
        }
    }

    @Override
    public boolean isOpen() {
        return file != null;
    }

    @Override
    public void close() throws Exception {
        file.close();
        file = null;
        super.close();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Set filter that should be applied to events that should be written to
     * file
     *
     * @param filter
     */
    public void setFilter(Predicate<Event> filter) {
        this.filter = filter;
    }

    @Override
    protected void pushDirect(Event event) throws StorageException {
        if (filter == null || filter.test(event)) {
            try {
                file.append(event.toString().getBytes(CHARSET));
                file.append(NEWLINE);
            } catch (IOException ex) {
                throw new StorageException(ex);
            }
        }
    }

    /**
     * Get the whole log as a String
     *
     * @return
     */
    public String getLog() {
        return new String(file.getData().array(), CHARSET);
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

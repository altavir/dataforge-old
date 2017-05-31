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
import hep.dataforge.io.IOUtils;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.JSONMetaWriter;
import hep.dataforge.storage.loaders.AbstractLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author darksnake
 */
public class FileEventLoader extends AbstractLoader implements EventLoader {

    private static final byte[] NEWLINE = {'\r', '\n'};

    public static FileEventLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), readOnly)) {
            return fromEnvelope(storage, envelope);
        }
    }

    public static FileEventLoader fromEnvelope(Storage storage, FileEnvelope envelope) throws Exception {
        if (FileStorageEnvelopeType.validate(envelope, EVENT_LOADER_TYPE)) {
            FileEventLoader res = new FileEventLoader(envelope.getFile().getURL().toString(),
                    storage, FilenameUtils.getBaseName(envelope.getFile().getName().getBaseName()),
                    envelope.meta());
            res.setReadOnly(envelope.isReadOnly());
            return res;
        } else {
            throw new StorageException("Is not a valid point loader file");
        }
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
    public boolean pushEvent(Event event) throws StorageException {
        if (filter == null || filter.test(event)) {
            try {
                String eventString = new JSONMetaWriter(false).writeString(event.meta());
                getFile().append(eventString.getBytes(IOUtils.UTF8_CHARSET));
                getFile().append(NEWLINE);
                return true;
            } catch (Exception ex) {
                throw new StorageException(ex);
            }
        }else {
            return false;
        }
    }

//    /**
//     * Get the whole log as a String
//     *
//     * @return
//     */
//    public String getChronicle() {
//        return new String(getFile().getData().array(), CHARSET);
//    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the file
     */
    private FileEnvelope getFile() throws Exception {
        if (file == null) {
            open();
        }
        return file;
    }
}

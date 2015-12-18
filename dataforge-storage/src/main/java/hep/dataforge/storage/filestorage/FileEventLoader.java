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

import hep.dataforge.meta.Meta;
import hep.dataforge.events.Event;
import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import hep.dataforge.storage.loaders.AbstractEventLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.function.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 *
 * @author darksnake
 */
public class FileEventLoader extends AbstractEventLoader {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final byte[] NEWLINE = {'\r', '\n'};

    public static FileEventLoader fromLocalFile(Storage storage, File file, boolean readOnly) throws IOException, ParseException, StorageException {
        return fromFile(storage, new DefaultLocalFileProvider().findLocalFile(file), readOnly);
    }

    public static FileEventLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws IOException, ParseException, StorageException {
        FileEnvelope envelope = new FileEnvelope(file, readOnly);
        if (isValidFileEventLoaderEnvelope(envelope)) {
            return new FileEventLoader(envelope, storage, FilenameUtils.getBaseName(file.getName().getBaseName()), envelope.meta());
        } else {
            throw new StorageException("Is not a valid event loader file");
        }
    }

    public static boolean isValidFileEventLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.EVENT_LOADER_TYPE_CODE;
    }

    private final FileEnvelope envelope;
    private Predicate<Event> filter;

    public FileEventLoader(FileEnvelope envelope, Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
        this.envelope = envelope;
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
                envelope.append(event.toString().getBytes(CHARSET));
                envelope.append(NEWLINE);
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
        return new String(envelope.getData().array(), CHARSET);
    }

}

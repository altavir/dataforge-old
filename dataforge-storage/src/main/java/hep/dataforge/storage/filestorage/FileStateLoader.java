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

import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import hep.dataforge.storage.loaders.AbstractStateLoader;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

/**
 * A file implementation of state loader
 *
 * @author Alexander Nozik
 */
public class FileStateLoader extends AbstractStateLoader {

    public static FileStateLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), readOnly)) {
            if (isValidFileStateLoaderEnvelope(envelope)) {
                FileStateLoader res = new FileStateLoader(file.getURL().toString(),
                        storage, FilenameUtils.getBaseName(file.getName().getBaseName()),
                        envelope.meta());
                res.setReadOnly(readOnly);
                return res;
            } else {
                throw new StorageException("Is not a valid point loader file");
            }
        }
    }

    public static boolean isValidFileStateLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.STATE_LOADER_TYPE_CODE;
    }

    private final String filePath;
    private FileEnvelope file;

    public FileStateLoader(String filePath, Storage storage, String name, Meta annotation) throws IOException, StorageException {
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
    protected void commit() throws StorageException {
        try {
            getFile().clearData();
            for (Map.Entry<String, Value> entry : states.entrySet()) {
                getFile().append(String.format("%s=%s;\r\n", entry.getKey(), entry.getValue().stringValue()).getBytes(Charset.forName("UTF-8")));
            }
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    protected synchronized void update() throws StorageException {
        try {
            getFile().resetPos();
            states.clear();
            while (getFile().readerPos() < getFile().eofPos()) {
                String line = getFile().readLine().trim();
                if (!line.isEmpty()) {
                    Matcher match = Pattern.compile("(?<key>[^=]*)\\s*=\\s*(?<value>.*);").matcher(line);
                    if (match.matches()) {
                        String key = match.group("key");
                        Value value = Value.of(match.group("value"));
                        states.put(key, value);
                    }
                }
            }
            upToDate = true;
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    /**
     * @return the file
     */
    private FileEnvelope getFile() throws Exception {
        if(file == null){
            open();
        }
        return file;
    }
}

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
import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import hep.dataforge.storage.loaders.AbstractStateLoader;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 * A file implementation of state loader
 * @author Alexander Nozik
 */
public class FileStateLoader extends AbstractStateLoader {

    public static FileStateLoader fromLocalFile(Storage storage, File file, boolean readOnly) throws IOException, ParseException, StorageException {
        return fromFile(storage, new DefaultLocalFileProvider().findLocalFile(file),readOnly);
    }

    public static FileStateLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws IOException, ParseException, StorageException {
        FileEnvelope envelope = new FileEnvelope(file,readOnly);
        if (isValidFileStateLoaderEnvelope(envelope)) {
            return new FileStateLoader(envelope, storage, FilenameUtils.getBaseName(file.getName().getBaseName()), envelope.meta());
        } else {
            throw new StorageException("Is not a valid point loader file");
        }
    }

    public static boolean isValidFileStateLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.STATE_LOADER_TYPE_CODE;
    }

    private final FileEnvelope envelope;

    public FileStateLoader(FileEnvelope envelope, Storage storage, String name, Meta annotation) throws IOException, StorageException {
        super(storage, name, annotation);
        this.envelope = envelope;
    }

    @Override
    protected void commit() throws StorageException {
        try {
            envelope.clearData();
            for (Map.Entry<String, Value> entry : states.entrySet()) {
                envelope.append(String.format("%s=%s;\r\n", entry.getKey(), entry.getValue().stringValue()).getBytes(Charset.forName("UTF-8")));
            }
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    protected synchronized void update() throws StorageException {
        try {
            envelope.resetPos();
            states.clear();
            while (envelope.readerPos() < envelope.eofPos()) {
                String line = envelope.readLine().trim();
                if (!line.isEmpty()) {
                    Matcher match = Pattern.compile("(?<key>[^=]*)\\s*=\\s*(?<value>.*);").matcher(line);
                    if(match.matches()){
                        String key = match.group("key");
                        Value value = Value.of(match.group("value"));
                        states.put(key, value);
                    }
                }
            }
            upToDate = true;
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }
}

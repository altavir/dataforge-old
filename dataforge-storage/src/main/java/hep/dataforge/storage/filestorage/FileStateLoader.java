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
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.loaders.AbstractStateLoader;
import hep.dataforge.values.Value;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A file implementation of state loader
 *
 * @author Alexander Nozik
 */
public class FileStateLoader extends AbstractStateLoader {

    public static FileStateLoader fromEnvelope(Storage storage, FileEnvelope envelope) throws Exception {
        if (FileStorageEnvelopeType.validate(envelope, STATE_LOADER_TYPE)) {
            FileStateLoader res = new FileStateLoader(envelope.getFile(),
                    storage, FilenameUtils.getBaseName(envelope.getFile().getFileName().toString()),
                    envelope.meta());
            res.setReadOnly(envelope.isReadOnly());
            return res;
        } else {
            throw new StorageException("Is not a valid state loader file");
        }
    }

    private final Path path;
    private FileEnvelope file;

    public FileStateLoader(Path path, Storage storage, String name, Meta annotation) throws IOException, StorageException {
        super(storage, name, annotation);
        this.path = path;
    }

    @Override
    public void open() throws Exception {
        if (this.meta == null) {
            this.meta = getFile().meta();
        }
        if (!isOpen()) {
            file = new FileEnvelope(path, isReadOnly());
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(getFile().getData().getStream()));
            states.clear();
            reader.lines().forEach(line -> {
                if (!line.isEmpty()) {
                    Matcher match = Pattern.compile("(?<key>[^=]*)\\s*=\\s*(?<value>.*);").matcher(line);
                    if (match.matches()) {
                        String key = match.group("key");
                        Value value = Value.of(match.group("value"));
                        states.put(key, value);
                    }
                }
            });
            upToDate = true;
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
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

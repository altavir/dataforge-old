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

import hep.dataforge.data.DataFormat;
import hep.dataforge.data.DataParser;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.ListDataSet;
import hep.dataforge.data.SimpleDataParser;
import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Query;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.EnvelopeCodes;
import hep.dataforge.storage.loaders.AbstractPointLoader;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 * Text file point loader
 *
 * @author Alexander Nozik
 */
//TODO add FileMonitor for read only loaders
public class FilePointLoader extends AbstractPointLoader implements Iterable<DataPoint> {

    public static FilePointLoader fromLocalFile(Storage storage, File file, boolean readOnly) throws IOException, ParseException, StorageException {
        return fromFile(storage, new DefaultLocalFileProvider().findLocalFile(file), readOnly);
    }

    public static FilePointLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws IOException, ParseException, StorageException {
        FileEnvelope envelope = new FileEnvelope(file, readOnly);
        if (isValidFilePointLoaderEnvelope(envelope)) {
            return new FilePointLoader(envelope, storage, FilenameUtils.getBaseName(file.getName().getBaseName()), envelope.meta());
        } else {
            throw new StorageException("Is not a valid point loader file");
        }
    }

    public static boolean isValidFilePointLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.POINT_LOADER_TYPE_CODE;
    }

    private FileEnvelope helper;
    private NavigableMap<Value, Integer> index = Collections.synchronizedNavigableMap(new TreeMap<>());
    private DataFormat format;

    public FilePointLoader(FileEnvelope helper, Storage storage, String name, Meta annotation) throws IOException, StorageException {
        super(storage, name, annotation);
        this.helper = helper;
    }

    /**
     * Update index from last indexed position to the end of file
     *
     * @throws IOException
     * @throws StorageException
     */
    private synchronized void updateIndex() throws IOException, StorageException {
        if (index.isEmpty()) {
            helper.resetPos();
        } else {
            helper.seek(Collections.max(index.values()));
        }

        DataParser parser = null;
        buildFormat();
        if (format != null) {
            parser = new SimpleDataParser(format);
        }

        while (helper.readerPos() < helper.eofPos()) {
            //Remember the position of line start
            int curPos = (int) helper.readerPos();
            String line = helper.readLine().trim();
            if (!line.startsWith("#") && !line.isEmpty()) {
                if (parser == null) {
                    throw new StorageException("The data format is not defined");
                }
                DataPoint point = parser.parse(line);

                //First building parser to define names, then Format to define types
                if (format == null) {
                    format = DataFormat.forPoint(point);
                }

                if (indexField().equals(DEFAULT_INDEX_FIELD)) {
                    if (index.isEmpty()) {
                        index.put(Value.of(0), (int) helper.readerPos());
                    } else {
                        index.put(Value.of(index.lastKey().intValue() + 1), curPos);
                    }
                } else {
                    index.put(point.getValue(indexField()), curPos);
                }
            } else if (line.startsWith("#f")) {
                //Format string starts with #f
                parser = new SimpleDataParser(line.substring(2));
            }
        }
    }

    /**
     * Check if index is valid and update if needed. Index is assumed valid if
     * the reader position is in the end of file
     */
    private synchronized void checkIndex() throws StorageException {
        try {
            if (helper.readerPos() != helper.eofPos()) {
                updateIndex();
            }
        } catch (IOException ex) {
            throw new StorageException("Can't update index for pointLoader", ex);
        }
    }

    private DataFormat buildFormat() {
        if (format == null) {
            if (meta().hasNode("format")) {
                format = DataFormat.fromMeta(meta().getNode("format"));
            } else if (meta().hasValue("format")) {
                format = DataFormat.forNames(meta().getStringArray("format"));
            } else {
                format = null;
            }
        }
        return format;

    }

    @Override
    public DataPoint pull(Value value) throws StorageException {
        checkIndex();
        int pos = index.floorEntry(value).getValue();
        return readAtPos(pos);
    }

    @Override
    public DataSet pull(Value from, Value to, int maxItems) throws StorageException {
        checkIndex();
        if (from == null || from.isNull()) {
            from = index.firstKey();
        }
        if (to == null || to.isNull()) {
            to = index.lastKey();
        }

        List<Integer> reducedIndex = index.subMap(from, to).values().stream().collect(Collectors.toList());

        Collections.reverse(reducedIndex);

        List<DataPoint> points = reducedIndex.stream().limit(maxItems)
                .<DataPoint>map((pos) -> readAtPos(pos))
                .collect(Collectors.toList());
        //TODO add annotation to DataSet here
        return new ListDataSet("pullResult", null, points);
    }

    public DataPoint readAtPos(int pos) {
        try {
            return new SimpleDataParser(buildFormat()).parse(helper.readLine(pos));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    synchronized public void pushPoint(DataPoint dp) throws StorageException {
        checkIndex();
        //Building format if it is not defined in annotation
        if (buildFormat() == null) {
            format = DataFormat.forPoint(dp);
        }

        try {
            if (index.isEmpty()) {
                helper.append(("#?" + buildFormat().formatCaption() + "\r\n").getBytes());
            }

            String indexField = indexField();
            int pos = (int) helper.eofPos();
            if (indexField.equals(DEFAULT_INDEX_FIELD)) {
                if (index.isEmpty()) {
                    index.put(Value.of(0), pos);
                } else {
                    index.put(Value.of(index.lastKey().intValue() + 1), pos);
                }
            } else if (dp.hasValue(indexField)) {
                Value indexValue = dp.getValue(indexField());
                if (index.containsKey(indexValue)) {
                    throw new StorageException("The point with given index is already present in the loader");
                }

                index.put(indexValue, (int) helper.eofPos());
            } else {
                throw new StorageException("Index field is not present in data point");
            }

            helper.append(("  " + buildFormat().format(dp) + "\r\n").getBytes());
        } catch (IOException ex) {
            throw new StorageException("IOexception during push", ex);
        }
    }

    @Override
    public DataSet pull(Query query) throws StorageException {
        checkIndex();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataSet asDataSet() throws StorageException {
        return new ListDataSet(getName(), meta(), this, buildFormat());
    }

    @Override
    public Iterator<DataPoint> iterator() {
        try {
            checkIndex();
        } catch (StorageException ex) {
            throw new RuntimeException("Can't update index", ex);
        }
        final Iterator<Integer> lineIterator = index.values().iterator();
        return new Iterator<DataPoint>() {

            @Override
            public boolean hasNext() {
                return lineIterator.hasNext();
            }

            @Override
            public DataPoint next() {
                return readAtPos(lineIterator.next());
            }
        };
    }

    @Override
    public boolean isEmpty() {
        try {
            checkIndex();
        } catch (StorageException ex) {
            throw new RuntimeException("Can't update index", ex);
        }
        return index.isEmpty();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.points.Format;
import hep.dataforge.points.DataPoint;
import hep.dataforge.points.ListPointSet;
import hep.dataforge.points.SimpleParser;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.LineIterator;
import static hep.dataforge.io.envelopes.Envelope.DATA_TYPE_KEY;
import static hep.dataforge.io.envelopes.Envelope.TYPE_KEY;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Names;
import hep.dataforge.storage.api.Index;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.DefaultIndex;
import hep.dataforge.storage.commons.EnvelopeCodes;
import hep.dataforge.storage.commons.ValueProviderIndex;
import hep.dataforge.storage.loaders.AbstractPointLoader;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.function.Supplier;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import hep.dataforge.points.PointSet;
import hep.dataforge.points.PointParser;

/**
 *
 * @author Alexander Nozik
 */
public class FilePointLoader extends AbstractPointLoader {

    public static FilePointLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), readOnly)) {
            if (isValidFilePointLoaderEnvelope(envelope)) {
                FilePointLoader res = new FilePointLoader(storage,
                        FilenameUtils.getBaseName(file.getName().getBaseName()),
                        envelope.meta(),
                        file.getURL().toString());
                res.setReadOnly(readOnly);
                return res;
            } else {
                throw new StorageException("Is not a valid point loader file");
            }
        }
    }

    public static boolean isValidFilePointLoaderEnvelope(FileEnvelope envelope) {
        return envelope.getProperties().get(TYPE_KEY).intValue() == EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE
                && envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.POINT_LOADER_TYPE_CODE;
    }

    private final String uri;
    private Format format;
    private PointParser parser;

    /**
     * An envelop used for pushing
     */
    private FileEnvelope envelope;

    public FilePointLoader(Storage storage, String name, Meta annotation, String uri) {
        super(storage, name, annotation);
        this.uri = uri;
    }

    public FilePointLoader(Storage storage, String name, String uri) {
        super(storage, name);
        this.uri = uri;
    }

    @Override
    public void open() throws Exception {
        if (this.meta == null) {
            this.meta = buildEnvelope(true).meta();
        }
        // read format from first line if it is not defined in meta
        if (getFormat() == null) {
            FileEnvelope reader = buildEnvelope(true);
            while (!reader.isEof()) {
                String line = reader.readLine();
                if (line.startsWith("#f")) {
                    format = new Format(Names.of(line.substring(2).trim().split("[^\\w']+")));
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        parser = null;
        format = null;
        if (envelope != null) {
            envelope.close();
            envelope = null;
        }
        super.close();
    }

    private FileEnvelope buildEnvelope(boolean readOnly) throws StorageException {
        try {
            return new FileEnvelope(uri, readOnly);
        } catch (IOException | ParseException ex) {
            throw new StorageException(ex);
        }
    }

    /**
     * Get writeable reusable single access envelope for this loader
     *
     * @return
     * @throws StorageException
     */
    private FileEnvelope getEnvelope() throws StorageException {
        if (this.envelope == null) {
            this.envelope = buildEnvelope(false);
        }
        return this.envelope;
    }

    private Format getFormat() {
        if (format == null) {
            if (meta().hasNode("format")) {
                format = Format.fromMeta(meta().getNode("format"));
            } else if (meta().hasValue("format")) {
                format = Format.forNames(meta().getStringArray("format"));
            } else {
                format = null;
            }
        }
        return format;
    }

    private PointParser getParser() {
        if (parser == null) {
            parser = new SimpleParser(getFormat());
        }
        return parser;
    }

    @Override
    protected void pushPoint(DataPoint dp) throws StorageException {
        try {
            if (!getEnvelope().hasData()) {
                getEnvelope().appendLine(getFormat().formatCaption());
            }
            String str = getFormat().format(dp);
            getEnvelope().appendLine(str);
        } catch (IOException ex) {
            throw new StorageException("Error while openning an envelope", ex);
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            return (envelope != null && !envelope.hasData()) || !buildEnvelope(true).hasData();
        } catch (StorageException ex) {
            throw new RuntimeException("Can't access loader envelope", ex);
        }
    }

    @Override
    public PointSet asDataSet() throws StorageException {
        return new ListPointSet(this, getFormat());
    }

    @Override
    public Iterator<DataPoint> iterator() {
        try {
            FileEnvelope reader = buildEnvelope(true);
            LineIterator iterator = new LineIterator(reader.getDataStream(), "UTF-8");
            return new Iterator<DataPoint>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public DataPoint next() {
                    return transform(iterator.next());
                }
            };
        } catch (StorageException | IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private DataPoint transform(String line) {
        return getParser().parse(line);
    }

//    @Override
//    public Index<DataPoint> buildIndex(Meta indexMeta) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    public Index<DataPoint> getMapIndex(String name) {
        return new FilePointIndex(name, getStorage().getContext(), () -> {
            try {
                return getEnvelope();
            } catch (StorageException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Index<DataPoint> getIndex(String name) {
        if (name == null || name.isEmpty()) {
            return new DefaultIndex<>(this);
        } else {
            return new ValueProviderIndex<>(this, name);
        }
    }

    private class FilePointIndex extends FileMapIndex<DataPoint> {

        private final String valueName;

        public FilePointIndex(String valueName, Context context, Supplier<FileEnvelope> sup) {
            super(context, sup);
            this.valueName = valueName;
        }

        @Override
        protected Value getIndexedValue(DataPoint entry) {
            return entry.getValue(valueName);
        }

        @Override
        protected String indexFileName() {
            return getStorage().getName() + "/index_" + getName() + "_" + valueName;
        }

        @Override
        protected DataPoint readEntry(String str) {
            return FilePointLoader.this.transform(str);
        }

    }
}

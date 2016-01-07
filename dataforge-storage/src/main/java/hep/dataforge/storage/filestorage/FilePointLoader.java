/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.data.DataFormat;
import hep.dataforge.data.DataParser;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.ListDataSet;
import hep.dataforge.data.SimpleDataParser;
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
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;

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
    private DataFormat format;
    private DataParser parser;

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
        if(this.meta == null){
            this.meta =  buildEnvelope(true).meta();
        }
        // read format from first line if it is not defined in meta
        if (getFormat() == null) {
            FileEnvelope reader = buildEnvelope(true);
            while (!reader.isEof()) {
                String line = reader.readLine();
                if (line.startsWith("#f")) {
                    format = new DataFormat(Names.of(line.substring(2).trim().split("[^\\w']+")));
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        parser = null;
        format = null;
        envelope.close();
        envelope = null;
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

    private DataFormat getFormat() {
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

    private DataParser getParser() {
        if (parser == null) {
            parser = new SimpleDataParser(getFormat());
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
    public DataSet asDataSet() throws StorageException {
        return new ListDataSet(getName(), meta(), this, getFormat());
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

    @Override
    public Index<DataPoint> buildIndex(Meta indexMeta) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Index<DataPoint> getMapIndex(String name) {
        return null;
    }

    @Override
    public Index<DataPoint> getIndex(String name) {
        Index<DataPoint> mapIndex = getMapIndex(name);
        if (mapIndex != null) {
            return mapIndex;
        } else if (name == null || name.isEmpty()) {
            return new DefaultIndex<>(this);
        } else {
            return new ValueProviderIndex<>(this, name);
        }
    }

}

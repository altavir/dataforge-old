/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.IOUtils;
import hep.dataforge.io.LineIterator;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Names;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.storage.commons.DefaultIndex;
import hep.dataforge.storage.loaders.AbstractTableLoader;
import hep.dataforge.tables.MetaTableFormat;
import hep.dataforge.tables.PointParser;
import hep.dataforge.tables.SimpleParser;
import hep.dataforge.tables.TableFormat;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * @author Alexander Nozik
 */
public class FileTableLoader extends AbstractTableLoader {

    public static FileTableLoader fromEnvelope(Storage storage, FileEnvelope envelope) throws Exception {
        if (FileStorageEnvelopeType.validate(envelope, POINT_LOADER_TYPE)) {
            FileTableLoader res = new FileTableLoader(storage,
                    FilenameUtils.getBaseName(envelope.getFile().getFileName().toString()),
                    envelope.meta(),
                    envelope.getFile());
            res.setReadOnly(envelope.isReadOnly());
            return res;
        } else {
            throw new StorageException("Is not a valid point loader file");
        }
    }

    private final Path path;
    //FIXME move to abstract
    private TableFormat format;
    private PointParser parser;

    /**
     * An envelope used for pushing
     */
    private FileEnvelope envelope;

    public FileTableLoader(Storage storage, String name, Meta meta, Path path) {
        super(storage, name, meta);
        this.path = path;
    }

    @Override
    public void open() throws Exception {
        if (this.meta == null) {
            this.meta = buildEnvelope(true).meta();
        }
        // read format from first line if it is not defined in meta
        if (getFormat() == null) {
            try (FileEnvelope envelope = buildEnvelope(true)) {
                new BufferedReader(Channels.newReader(envelope.getData().getChannel(), "UTF8"))
                        .lines()
                        .findFirst()
                        .ifPresent(line -> {
                            if (line.startsWith("#f")) {
                                format = MetaTableFormat.forNames(Names.of(line.substring(2).trim().split("[^\\w']+")));
                            }
                        });
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

    private FileEnvelope buildEnvelope(boolean readOnly) {
        return FileEnvelope.open(path, readOnly);
    }

    /**
     * Get writeable reusable single access envelope for this loader
     *
     * @return
     */
    private FileEnvelope getEnvelope() {
        if (this.envelope == null) {
            this.envelope = buildEnvelope(false);
        }
        return this.envelope;
    }

    @Override
    public TableFormat getFormat() {
        if (format == null) {
            if (meta().hasMeta("format")) {
                format = new MetaTableFormat(meta().getMeta("format"));
            } else if (meta().hasValue("format")) {
                format = MetaTableFormat.forNames(meta().getStringArray("format"));
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
    protected void pushPoint(Values dp) throws StorageException {
        try {
            if (!getEnvelope().hasData()) {
                getEnvelope().appendLine(IOUtils.formatCaption(getFormat()));
            }
            String str = IOUtils.formatDataPoint(getFormat(), dp);
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

    @NotNull
    @Override
    public Iterator<Values> iterator() {
        try {
            FileEnvelope reader = buildEnvelope(true);
            LineIterator iterator = new LineIterator(reader.getData().getStream(), "UTF-8");
            return new Iterator<Values>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Values next() {
                    return transform(iterator.next());
                }
            };
        } catch (StorageException | IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private Values transform(String line) {
        return getParser().parse(line);
    }

    @Override
    public ValueIndex<Values> buildIndex(String name) {
        if (name == null || name.isEmpty()) {
            //use point number index
            return new DefaultIndex<>(this);
        } else {
            return new FilePointIndex(name, getStorage().getContext(), this::getEnvelope);
        }
    }


    private class FilePointIndex extends FileMapIndex<Values> {

        private final String valueName;

        public FilePointIndex(String valueName, Context context, Supplier<FileEnvelope> sup) {
            super(context, sup);
            this.valueName = valueName;
        }

        @Override
        protected Value getIndexedValue(Values entry) {
            return entry.getValue(valueName);
        }

        @Override
        protected String indexFileName() {
            if (getStorage().isAnonimous()) {
                return getName() + "_" + valueName;
            } else {
                return getStorage().getName() + "/" + getName() + "_" + valueName;
            }
        }

        @Override
        protected Values readEntry(String str) {
            return FileTableLoader.this.transform(str);
        }

    }
}

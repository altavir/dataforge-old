/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.storage.commons.DefaultIndex;
import hep.dataforge.storage.commons.ValueProviderIndex;
import hep.dataforge.values.ValueProvider;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.function.Function;

/**
 * A factory for file indexes
 *
 * @author Alexander Nozik
 */
public class FileIndexFactory implements Encapsulated {

    private final Context context;
    private String uri;
    private FileEnvelope envelope;

    public FileIndexFactory(Context context, String uri) {
        this.context = context;
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Uri is empty");
        }
        this.uri = uri;
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Build an index which uses entry number for search
     *
     * @param <T>
     * @param transformation transformation from string to a given object type
     * @return
     */
    public <T> DefaultIndex<T> buildDefaultIndex(Function<String, T> transformation) {
        return new DefaultIndex<>(iterable(transformation));
    }

    /**
     * Build index for elements which implements ValueProvider interface
     *
     * @param <T>
     * @param valueName
     * @param transformation
     * @return
     */
    public <T extends ValueProvider> ValueProviderIndex<T> buildProviderStreamIndex(String valueName, Function<String, T> transformation) {
        return new ValueProviderIndex<>(iterable(transformation), valueName);
    }

    protected <T> Iterable<T> iterable(Function<String, T> transformation) {
        return () -> buildIterator(transformation);
    }

    protected <T> Iterator<T> buildIterator(Function<String, T> transformation) {
        try {
            FileEnvelope env = getEvelope();
            return env.getData().lines().map(transformation).iterator();
        } catch (IOException ex) {
            invalidate();
            throw new RuntimeException("Cant operate file envelope", ex);
        }
    }

    /**
     * Get or build envelope from uri
     *
     * @return
     * @throws IOException
     */
    private FileEnvelope getEvelope() throws IOException {
        if (envelope == null) {
            Path file = Paths.get(uri);
            if (Files.isReadable(file)) {
                envelope = FileEnvelope.open(uri, true);
            } else {
                invalidate();
                throw new RuntimeException("Can't read file " + uri);
            }
        }
        return envelope;
    }

    /**
     * Invalidate this factory and force it to reload file envelope
     */
    public void invalidate() {
        if (envelope != null) {
            try {
                envelope.close();
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).error("Can't close the file envelope", ex);
            }
            envelope = null;
        }
    }

}

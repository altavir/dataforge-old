/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.AbstractEnvelopeLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author Alexander Nozik
 */
public class FileEnvelopeLoader extends AbstractEnvelopeLoader {

//    public static FileEnvelopeLoader fromFile(Storage storage, FileObject file, boolean readOnly) throws Exception {
//        try (FileEnvelope envelope = new FileEnvelope(file.getURL().toString(), readOnly)) {
//            if (FileStorageEnvelopeType.validate(envelope)) {
//                FileEnvelopeLoader res = new FileEnvelopeLoader(storage,
//                        FilenameUtils.getBaseName(file.getName().getBaseName()),
//                        envelope.meta(),
//                        file.getURL().toString());
//                res.setReadOnly(readOnly);
//                return res;
//            } else {
//                throw new StorageException("Is not a valid point loader file");
//            }
//        }
//    }

    private final String filePath;
    private FileEnvelope file;

    public FileEnvelopeLoader(Storage storage, String name, Meta meta, String uri) {
        super(storage, name, meta);
        this.filePath = uri;
    }

    private FileEnvelope getFile() throws Exception {
        if (file == null) {
            open();
        }
        return file;
    }

    @Override
    public boolean isEmpty() {
        try {
            return !getFile().hasData();
        } catch (Exception ex) {
            throw new RuntimeException("Can't access loader envelope", ex);
        }
    }

    @Override
    public void open() throws Exception {
        if (this.meta == null) {
            this.meta = getFile().meta();
        }
        if (!isOpen()) {
            file = FileEnvelope.open(filePath, isReadOnly());
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
    public void push(Envelope env) throws StorageException {
        checkOpen();
        if (!isReadOnly()) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DefaultEnvelopeWriter.instance.write(baos, env);
                file.append(baos.toByteArray());
            } catch (IOException ex) {
                throw new StorageException("Can't push envelope to loader", ex);
            }
        } else {
            throw new StorageException("The loader is read only");
        }
    }

    @Override
    public Iterator<Envelope> iterator() {
        checkOpen();
        InputStream st;
        try {
            st = file.getData().getStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new Iterator<Envelope>() {
            @Override
            public boolean hasNext() {
                try {
                    return st.available() > 0;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public Envelope next() {
                try {
                    return DefaultEnvelopeReader.INSTANCE.readWithData(st);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

}

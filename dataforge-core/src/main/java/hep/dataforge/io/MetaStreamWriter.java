/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.meta.Meta;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * The writer of meta to stream in some text or binary format
 *
 * @author Alexander Nozik
 */
public interface MetaStreamWriter {

    /**
     * write Meta object to the giver OuputStream using given charset (if it is
     * possible)
     *
     * @param stream
     * @param meta
     * @param charset a charset for this write operation if null, than default
     * charset is used
     */
    void write(OutputStream stream, Meta meta, Charset charset);

    default String writeString(Meta meta) {
        return writeString(meta, null);
    }

    default String writeString(Meta meta, Charset charset) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos, meta, charset);
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    default void writeToFile(File file, Meta meta, Charset charset) throws FileNotFoundException {
        write(new FileOutputStream(file), meta, charset);
    }
}

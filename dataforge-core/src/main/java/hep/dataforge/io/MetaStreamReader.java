/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.meta.MetaBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * The reader of stream containing meta in some text or binary format. By
 * default reader returns meta as-is without substitutions and includes so it
 * does not need context to operate.
 *
 * @author Alexander Nozik
 */
public interface MetaStreamReader {

    /**
     * read {@code length} bytes from InputStream and interpret it as
     * MetaBuilder. If {@code length < 0} then parse input stream until end of
     * annotation is found.
     * <p>
     * The returned build could be later transformed or
     * </p>
     *
     * @param stream a stream that should be read.
     * @param length a number of bytes from stream that should be read. Any
     * negative value .
     * @param charset an override charset for this read operation. Null value is
     * ignored
     * @throws java.io.IOException if any.
     * @throws java.text.ParseException if any.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    MetaBuilder read(InputStream stream, long length, Charset charset) throws IOException, ParseException;
    
    /**
     * Decide if this reader accepts given file
     * 
     * @return 
     */
    boolean acceptsFile(File file);
    //TODO replace by nio or vfs

    /**
     * Read the Meta from file. The whole file is considered to be Meta file.
     *
     * @param file
     * @param charset
     * @return
     * @throws IOException
     * @throws ParseException
     */
    default MetaBuilder readFile(File file, Charset charset) throws IOException, ParseException {
        return read(new FileInputStream(file), file.length(), charset);
    }

    /**
     * Read Meta from string
     *
     * @param string
     * @param charset overrides string encoding if present
     * @return
     * @throws IOException
     * @throws ParseException
     */
    default MetaBuilder readString(String string, Charset charset) throws IOException, ParseException {
        byte[] bytes;
        if (charset != null) {
            bytes = string.getBytes(charset);
        } else {
            bytes = string.getBytes();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return read(bais, bytes.length, charset);
    }
}

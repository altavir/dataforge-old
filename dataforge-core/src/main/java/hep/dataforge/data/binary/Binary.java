/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * An interface to represent something that one can read binary data from in a
 * blocking or non-blocking way. This interface is intended for read access
 * only.
 *
 * @author Alexander Nozik
 */
public interface Binary {
    
    /**
     * Read binary content to single ByteBuffer
     * @param binary
     * @return 
     */
    static ByteBuffer readToBuffer(Binary binary) throws IOException{
        if(binary.size() > 0){
            ByteBuffer buffer = ByteBuffer.allocate((int) binary.size());
            binary.getChannel().read(buffer);
            return buffer;
        } else {
            throw new IOException("Can not convert binary of undefined size to buffer");
        }
    }

    /**
     * Get blocking input stream for this binary
     *
     * @return
     */
    InputStream getStream() throws IOException;

    /**
     * Get non-blocking byte channel
     *
     * @return
     */
    ReadableByteChannel getChannel() throws IOException;

    /**
     * The size of this binary. Negative value corresponds to undefined size.
     *
     * @return
     * @throws IOException
     */
    long size() throws IOException;
}

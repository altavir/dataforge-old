/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class BufferedBinary implements Binary {

    private final byte[] buffer;

    public BufferedBinary(ByteBuffer buffer) {
        this.buffer = buffer.array();
    }

    public BufferedBinary(byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(buffer);
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return Channels.newChannel(getStream());
    }

    @Override
    public ByteBuffer getBuffer() throws IOException {
        return ByteBuffer.wrap(buffer);
    }

    @Override
    public long size() throws IOException {
        return buffer.length;
    }

}

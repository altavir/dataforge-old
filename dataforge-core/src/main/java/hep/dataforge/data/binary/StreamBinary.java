/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.WriteAbortedException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Supplier;

public class StreamBinary implements Binary {

    private final Supplier<InputStream> sup;
    private final int size;

    public StreamBinary(Supplier<InputStream> sup, int size) {
        this.sup = sup;
        this.size = size;
    }

    public StreamBinary(Supplier<InputStream> sup) {
        this.sup = sup;
        size = -1;
    }

    @Override
    public InputStream getStream() throws IOException {
        //TODO limit inputStream size
        return sup.get();
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return Channels.newChannel(sup.get());
    }

    @Override
    public long size() throws IOException {
        return size;
    }

    @NotNull
    private Object writeReplace() throws ObjectStreamException {
        try {
            return new BufferedBinary(getBuffer().array());
        } catch (IOException e) {
            throw new WriteAbortedException("Failed to get byte buffer", e);
        }
    }

}

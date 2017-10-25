/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;

public class FileBinary implements Binary {

    /**
     * File to create binary from
     */
    private final Path file;

    /**
     * dataOffset form beginning of file
     */
    private final int dataOffset;

    private final int size;

    public FileBinary(Path file, int dataOffset, int size) {
        this.file = file;
        this.dataOffset = dataOffset;
        this.size = size;
    }

    public FileBinary(Path file, int dataOffset) {
        this.file = file;
        this.dataOffset = dataOffset;
        this.size = -1;
    }

    public FileBinary(Path file) {
        this.file = file;
        this.dataOffset = 0;
        this.size = -1;
    }

    public InputStream getStream(int offset) throws IOException {
        InputStream stream = Files.newInputStream(file, READ);
        stream.skip(dataOffset + offset);
        return stream;
    }

    @Override
    public InputStream getStream() throws IOException {
        return getStream(0);
    }

    @Override
    public ByteChannel getChannel() throws IOException {
        return Files.newByteChannel(file, READ).position(dataOffset);
    }

    /**
     * Read a buffer with given dataOffset in respect to data block start and given size. If data size w
     *
     * @param offset
     * @param size
     * @return
     * @throws IOException
     */
    public ByteBuffer getBuffer(int offset, int size) throws IOException {
        try (FileChannel channel = FileChannel.open(file)) {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            channel.read(buffer, offset + dataOffset);
            return buffer;
        }
    }

    public ByteBuffer getBuffer(int start) throws IOException {
        return getBuffer(start, (int) (size() - start));
    }

    @Override
    public ByteBuffer getBuffer() throws IOException {
        return getBuffer(0, (int) size());
    }

    @Override
    public long size() throws IOException {
        return size >= 0 ? size : Files.size(file) - dataOffset;
    }

    public Stream<String> lines() throws IOException {
        return new BufferedReader(new InputStreamReader(getStream())).lines();
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

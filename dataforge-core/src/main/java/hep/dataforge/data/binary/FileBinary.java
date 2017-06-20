/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;

public class FileBinary implements Binary {

    /**
     * File to create binary from
     */
    private final Path file;

    /**
     * offset form beginning of file
     */
    private final int offset;

    private final int size;

    public FileBinary(Path file, int offset, int size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public FileBinary(Path file, int offset) {
        this.file = file;
        this.offset = offset;
        this.size = -1;
    }

    public FileBinary(Path file) {
        this.file = file;
        this.offset = 0;
        this.size = -1;
    }

    @Override
    public InputStream getStream() throws IOException {
        InputStream stream = Files.newInputStream(file, READ);
        if (offset > 0) {
            stream.skip(offset);
        }
        return stream;
    }

    @Override
    public SeekableByteChannel getChannel() throws IOException {
        return Files.newByteChannel(file, READ).position(offset);
    }

    @Override
    public long size() throws IOException {
        return size >= 0 ? size : Files.size(file) - offset;
    }

}

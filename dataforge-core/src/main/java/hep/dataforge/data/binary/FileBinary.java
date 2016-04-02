/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileBinary implements Binary {

    /**
     * File to create binary from
     */
    private final File file;

    /**
     * offset form beginning of file
     */
    private final int offset;

    private final int size;

    public FileBinary(File file, int offset, int size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public FileBinary(File file, int offset) {
        this.file = file;
        this.offset = offset;
        this.size = -1;
    }

    public FileBinary(File file) {
        this.file = file;
        this.offset = 0;
        this.size = -1;
    }

    @Override
    public FileInputStream getStream() throws IOException {
        FileInputStream stream = new FileInputStream(file);
        if (offset > 0) {
            stream.skip(offset);
        }
        return stream;
    }

    @Override
    public FileChannel getChannel() throws IOException {
        return getStream().getChannel();
    }

    @Override
    public long size() throws IOException {
        return size >= 0 ? size : file.length() - offset;
    }

}

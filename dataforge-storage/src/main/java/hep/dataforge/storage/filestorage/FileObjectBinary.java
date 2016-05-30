/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.data.binary.Binary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.commons.vfs2.FileObject;

/**
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class FileObjectBinary implements Binary {

    /**
     * File to create binary from
     */
    private final FileObject file;

    /**
     * offset form beginning of file
     */
    private final int offset;

    private final int size;

    public FileObjectBinary(FileObject file, int offset, int size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public FileObjectBinary(FileObject file, int offset) {
        this.file = file;
        this.offset = offset;
        this.size = -1;
    }

    public FileObjectBinary(FileObject file) {
        this.file = file;
        this.offset = 0;
        this.size = -1;
    }

    @Override
    public InputStream getStream() throws IOException {
        InputStream stream = file.getContent().getInputStream();
        if (offset > 0) {
            stream.skip(offset);
        }
        return stream;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return Channels.newChannel(getStream());
    }

    @Override
    public long size() throws IOException {
        return size >= 0 ? size : file.getContent().getSize() - offset;
    }

}

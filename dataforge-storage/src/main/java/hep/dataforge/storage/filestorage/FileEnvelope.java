/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.data.binary.Binary;
import hep.dataforge.data.binary.FileBinary;
import hep.dataforge.io.envelopes.DefaultEnvelopeType;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeTag;
import hep.dataforge.io.envelopes.EnvelopeType;
import hep.dataforge.meta.Meta;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * A specific envelope to handle file storage format.
 *
 * @author Alexander Nozik
 */
public class FileEnvelope implements Envelope, AutoCloseable {

    public static final long INFINITE_DATA_SIZE = Integer.toUnsignedLong(-1);
    private static final String NEWLINE = "\r\n";

    private final boolean readOnly;
    private final String uri;
    private FileObject file;
    private Meta meta;
    private RandomAccessContent randomAccess;
    private EnvelopeTag tag;
    private EnvelopeType type = DefaultEnvelopeType.instance;

    public FileEnvelope(String uri, boolean readOnly) throws IOException, ParseException {
        this.uri = uri;
        this.readOnly = readOnly;
    }

    @Override
    public synchronized void close() throws Exception {
        tag = null;

        if (randomAccess != null) {
            LoggerFactory.getLogger(getClass()).trace("Closing FileEnvelope content " + uri);
            randomAccess.close();
            randomAccess = null;
        }
        if (file != null) {
            LoggerFactory.getLogger(getClass()).trace("Closing FileEnvelope FileObject " + uri);
            file.close();
            file = null;
        }
    }

    @Override
    public Binary getData() {
        ensureOpen();
        try {
            long dataSize = tag.getDataSize();
            if (dataSize == INFINITE_DATA_SIZE) {
                dataSize = getRandomAccess().length() - getDataOffset();
            }
            //getRandomAccess().seek(getDataOffset());
            if (file.isFile()) {
                File f = new File(file.getURL().getFile());
                return new FileBinary(f, (int) getDataOffset(), (int) dataSize);
            } else {
                return new FileObjectBinary(file, (int) getDataOffset(), (int) dataSize);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized Meta meta() {
        if (meta == null) {
            ensureOpen();
            try (InputStream stream = getFile().getContent().getInputStream()) {
                meta = type.getReader().read(stream).meta();
            } catch (Exception e) {
                throw new RuntimeException("Can't read meta from file Envelope", e);
            }
        }
        return meta;
    }

    public String readLine(int offset) throws IOException {
        getRandomAccess().seek(offset);
        return readLine();
    }

    /**
     * Read the line in current position unescaping new line symbols
     *
     * @return
     * @throws IOException
     */
    public String readLine() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte nextChar = getRandomAccess().readByte();
        while (getRandomAccess().getFilePointer() < getRandomAccess().length() && nextChar != '\r') {
            buffer.put(nextChar);
            nextChar = getRandomAccess().readByte();

            if (!buffer.hasRemaining()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + 1024);
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
        }
        return new String(buffer.array(), Charset.forName("UTF-8")).replace("\\n", NEWLINE);
    }

    public void seek(long pos) throws IOException {
        getRandomAccess().seek(pos);
    }

    public ByteBuffer readBlock(int offset, int length) throws IOException {
        getRandomAccess().seek(offset);
        ByteBuffer block = ByteBuffer.allocate(length);
        while (block.hasRemaining()) {
            block.put(getRandomAccess().readByte());
        }
//        content.getChannel().read(block);
        return block;
    }

    /**
     * Read everything above data itself
     *
     * @return
     * @throws IOException
     */
    private ByteBuffer getHeader() throws IOException {
        return readBlock(0, (int) getDataOffset());
    }

    /**
     * Create a new file with the same header, but without data.
     *
     * @throws IOException
     */
    public synchronized void clearData() throws IOException {
        ByteBuffer header = getHeader();
        try (OutputStream stream = getFile().getContent().getOutputStream(false)) {
            stream.write(header.array());
            setDataSize(0);
        }
    }

    public long readerPos() throws IOException {
        return getRandomAccess().getFilePointer();
    }

    /**
     * Reset file pointer to data start
     *
     * @throws IOException
     */
    public void resetPos() throws IOException {
        getRandomAccess().seek(getDataOffset());
    }

    private synchronized void setDataSize(int size) throws IOException {
        long offset = getRandomAccess().getFilePointer();
        getRandomAccess().seek(0);//seeking begin
        getRandomAccess().write(tag.byteHeader());
        getRandomAccess().seek(offset);//return to the initial position
        tag.setValue(DATA_LENGTH_KEY, size);//update property
    }

    /**
     * Append byte array to the end of file without escaping and update data
     * size envelope property
     *
     * @param bytes
     * @throws IOException
     */
    synchronized public void append(byte[] bytes) throws IOException {
        ensureOpen();
        if (isReadOnly()) {
            throw new IOException("Trying to write to readonly file " + uri);
        } else {
            getRandomAccess().seek(eofPos());
            getRandomAccess().write(bytes);
            setDataSize((int) (eofPos() - getRandomAccess().getFilePointer()));
        }
    }

    /**
     * Append a new line with escaped new line characters
     *
     * @param line
     * @throws IOException
     */
    public void appendLine(String line) throws IOException {
        append((line.replace("\n", "\\n") + NEWLINE).getBytes());
    }

    public long eofPos() throws IOException {
        return getRandomAccess().length();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getFilePath() {
        return uri;
    }

    public boolean isEof() {
        try {
            return readerPos() == eofPos();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean hasData() {
        try {
            return this.eofPos() > this.getDataOffset();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public BufferedInputStream getDataStream() throws IOException {
        BufferedInputStream stream = new BufferedInputStream(getFile().getContent().getInputStream());
        stream.skip(getDataOffset());
        return stream;
    }

    /**
     * @return the content
     */
    private synchronized RandomAccessContent getRandomAccess() {
        try {
            if (!getFile().getContent().isOpen()) {
                randomAccess = null;
            }
            if (randomAccess == null) {
                if (isReadOnly()) {
                    randomAccess = getFile().getContent().getRandomAccessContent(RandomAccessMode.READ);
                } else {
                    randomAccess = getFile().getContent().getRandomAccessContent(RandomAccessMode.READWRITE);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return randomAccess;
    }

    /**
     * @return the file
     */
    public synchronized FileObject getFile() throws IOException {
        if (this.file == null) {
            this.file = VFS.getManager().resolveFile(uri);
            if (!file.exists()) {
                throw new java.io.FileNotFoundException();
            }
        }
        return this.file;
    }

    /**
     * Setup envelope properties, data offset and data size
     *
     * @throws IOException
     */
    private void open() {
        try (InputStream stream = getFile().getContent().getInputStream()) {
            tag = EnvelopeTag.from(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the dataOffset
     */
    private long getDataOffset() {
        ensureOpen();
        return tag.getLength() + tag.getMetaSize();
    }

    /**
     * ensure envelope is initialized
     */
    private synchronized void ensureOpen() {
        if (!isOpen()) {
            open();
        }
    }

    public boolean isOpen() {
        return tag != null;
    }
}

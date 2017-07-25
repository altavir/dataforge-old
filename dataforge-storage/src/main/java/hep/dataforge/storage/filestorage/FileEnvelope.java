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

import hep.dataforge.data.binary.FileBinary;
import hep.dataforge.io.envelopes.*;
import hep.dataforge.meta.Meta;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

/**
 * A specific envelope to handle file storage format.
 *
 * @author Alexander Nozik
 */
public class FileEnvelope implements Envelope, AutoCloseable {
    public static final long INFINITE_DATA_SIZE = Integer.toUnsignedLong(-1);
    private static final String NEWLINE = "\r\n";

    /**
     * Create empty envelope with given meta
     *
     * @param path
     * @param meta
     * @return
     */
    public static FileEnvelope createEmpty(Path path, Meta meta) throws IOException {
        try (OutputStream stream = Files.newOutputStream(path, CREATE, WRITE)) {
            new DefaultEnvelopeWriter().write(stream, new EnvelopeBuilder().setMeta(meta));
        }
        return new FileEnvelope(path, false);
    }

    public static FileEnvelope open(Path path, boolean readOnly) {
        if (!Files.exists(path)) {
            throw new RuntimeException("File envelope does not exist");
        }
        return new FileEnvelope(path, readOnly);
    }

    public static FileEnvelope open(String uri, boolean readOnly) {
        return open(Paths.get(URI.create(uri)), readOnly);
    }

    private final boolean readOnly;
    private Path file;
    private Meta meta;
    private FileChannel readChannel;
    private FileChannel writeChannel;
    private EnvelopeTag tag;
    private EnvelopeType type = DefaultEnvelopeType.instance;

    private FileEnvelope(Path path, boolean readOnly) {
        this.file = path;
        this.readOnly = readOnly;
    }

    public Path getFile() {
        return file;
    }

    private FileChannel getReadChannel() throws IOException {
        if (readChannel == null || !readChannel.isOpen()) {
            readChannel = FileChannel.open(file, READ);
        }
        return readChannel;
    }


    private EnvelopeTag getTag() throws IOException {
        if (tag == null) {
            tag = EnvelopeTag.from(getReadChannel());
        }
        return tag;
    }


    @Override
    public synchronized void close() throws Exception {
        tag = null;
        meta = null;
        LoggerFactory.getLogger(getClass()).trace("Closing FileEnvelope " + file);
        if (readChannel != null) {
            readChannel.close();
            readChannel = null;
        }
        if (writeChannel != null) {
            writeChannel.close();
            writeChannel = null;
        }

    }

    @Override
    public FileBinary getData() {
        try {
            long dataSize = getTag().getDataSize();
            if (dataSize == INFINITE_DATA_SIZE) {
                dataSize = getReadChannel().size() - getDataOffset();
            }
            //getRandomAccess().seek(getDataOffset());
            return new FileBinary(file, (int) getDataOffset(), (int) dataSize);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized Meta meta() {
        if (meta == null) {
            try {
                SeekableByteChannel channel = getReadChannel();
                channel.position(getTag().getLength());
                ByteBuffer buffer = ByteBuffer.allocate(getTag().getMetaSize());
                channel.read(buffer);
                meta = getTag().getMetaType().getReader().readBuffer(buffer);
            } catch (Exception e) {
                throw new RuntimeException("Can't read meta from file Envelope", e);
            }
        }
        return meta;
    }

    /**
     * Read line starting at given offset
     *
     * @param offset
     * @return
     * @throws IOException
     */
    public synchronized String readLine(int offset) throws IOException {
        //TODO move to binary?
        getReadChannel().position(getDataOffset() + offset);
        try (Reader stream = Channels.newReader(getReadChannel(), "UTF-8")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nextChar = stream.read();
            while (getReadChannel().position() < getReadChannel().size() && nextChar != '\r') {
                buffer.write(nextChar);
                nextChar = stream.read();
            }
            return new String(buffer.toByteArray(), Charset.forName("UTF-8")).replace("\\n", NEWLINE);
        }
    }


    public ByteBuffer readBlock(int pos, int length) throws IOException {
        ByteBuffer block = ByteBuffer.allocate(length);
        getReadChannel().read(block, pos);
        return block;
    }

    /**
     * Create a new file with the same header, but without data.
     *
     * @throws IOException
     */
    public synchronized void clearData() throws IOException {
        ByteBuffer header = readBlock(0, (int) getDataOffset());
        try (SeekableByteChannel channel = Files.newByteChannel(file, WRITE, TRUNCATE_EXISTING)) {
            channel.write(header);
            setDataSize(channel, 0);
        }
    }

    private long readerPos() throws IOException {
        return getReadChannel().position();
    }

    /**
     * Reset file pointer to data start
     *
     * @throws IOException
     */
    private void resetPos() throws IOException {
        getReadChannel().position(getDataOffset());
    }

    private synchronized void setDataSize(SeekableByteChannel channel, int size) throws IOException {
        getTag().setValue(DATA_LENGTH_KEY, size);//update property
        long position = channel.position();
        channel.position(0);//seeking begin
        ByteBuffer buffer = getTag().byteHeader();
        buffer.position(0);
        int d = channel.write(buffer);
        channel.position(position);//return to the initial position
    }

    /**
     * Append byte array to the end of file without escaping and update data
     * size envelope property
     *
     * @param bytes
     * @throws IOException
     */
    synchronized public void append(ByteBuffer bytes) throws IOException {
        SeekableByteChannel channel = getWriteChannel();
        channel.position(eofPos());
        channel.write(bytes);
        setDataSize(channel, (int) (channel.size() - getDataOffset()));
    }

    private SeekableByteChannel getWriteChannel() throws IOException {
        if (writeChannel == null || !writeChannel.isOpen()) {
            if (isReadOnly()) {
                throw new IOException("Trying to write to readonly file " + file);
            }
            writeChannel = FileChannel.open(file, WRITE);
        }
        return writeChannel;
    }

    public synchronized void append(byte[] bytes) throws IOException {
        this.append(ByteBuffer.wrap(bytes));
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

    private long eofPos() throws IOException {
        return getReadChannel().size();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    private boolean isEof() {
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

    /**
     * @return the dataOffset
     */
    private long getDataOffset() throws IOException {
        return getTag().getLength() + getTag().getMetaSize();
    }

    public boolean isOpen() {
        return readChannel != null || writeChannel != null;
    }
}

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

import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Tag;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.slf4j.LoggerFactory;

/**
 * A specific envelope to handle file storage format.
 *
 * @author Alexander Nozik
 */
public class FileEnvelope implements Envelope, AutoCloseable {

    public static final long INFINITE_DATA_SIZE = Integer.toUnsignedLong(-1);
    private static final long DATA_SIZE_PROPERTY_OFFSET = 22;
    private static final String NEWLINE = "\r\n";

    private final boolean readOnly;
    private final String uri;
    private FileObject file;
    private Meta meta;
    private RandomAccessContent content;
    private Map<String, Value> properties;
    private long dataOffset;
    private long dataSize;

    public FileEnvelope(String uri, boolean readOnly) throws IOException, ParseException {
        this.uri = uri;
        this.readOnly = readOnly;
    }

    @Override
    public synchronized void close() throws Exception {
        if (content != null) {
            LoggerFactory.getLogger(getClass()).debug("Closing FileEnvelope content " + uri);
            content.close();
            content = null;
        }
        if (file != null) {
            LoggerFactory.getLogger(getClass()).debug("Closing FileEnvelope FileObject " + uri);
            file.close();
            file = null;
        }
    }

    @Override
    public ByteBuffer getData() {
        try {
            if (dataSize == INFINITE_DATA_SIZE) {
                dataSize = getContent().length() - dataOffset;
            }

            getContent().seek(dataOffset);

            return readBlock((int) dataOffset, (int) dataSize);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public Map<String, Value> getProperties() {
        if (properties == null) {
            try {
                getFile();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return properties;

    }

    public String readLine(int offset) throws IOException {
        getContent().seek(offset);
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
        byte nextChar = getContent().readByte();
        while (getContent().getFilePointer() < getContent().length() && nextChar != '\r') {
            buffer.put(nextChar);
            nextChar = getContent().readByte();

            if (!buffer.hasRemaining()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + 1024);
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
        }
        return new String(buffer.array(), Charset.forName("UTF-8")).replace("\\n", NEWLINE);
    }

    public void seek(long pos) throws IOException {
        getContent().seek(pos);
    }

    public ByteBuffer readBlock(int offset, int length) throws IOException {
        getContent().seek(offset);
        ByteBuffer block = ByteBuffer.allocate(length);
        while (block.hasRemaining()) {
            block.put(getContent().readByte());
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
        return readBlock(0, (int) dataOffset);
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
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public long readerPos() throws IOException {
        return getContent().getFilePointer();
    }

    /**
     * Reset file pointer to data start
     *
     * @throws IOException
     */
    public void resetPos() throws IOException {
        getContent().seek(dataOffset);
    }

    synchronized protected void setDataSize(int size) throws IOException {
        long offset = getContent().getFilePointer();
        getContent().seek(DATA_SIZE_PROPERTY_OFFSET);//seeking binary 
        getContent().writeInt(size); // write 4 bytes
        getContent().seek(offset);//return to the initial position
        properties.put(DATA_LENGTH_KEY, Value.of(size));//update property
    }

    /**
     * Append byte array to the end of file without escaping and update data
     * size envelope property
     *
     * @param bytes
     * @throws IOException
     */
    synchronized public void append(byte[] bytes) throws IOException {
        if (isReadOnly()) {
            throw new IOException("Trying to write to readonly file " + uri);
        } else {
            getContent().seek(eofPos());
            getContent().write(bytes);
            dataSize += bytes.length;
            setDataSize((int) (eofPos() - getContent().getFilePointer()));
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
        return getContent().length();
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
            return this.dataOffset == this.eofPos();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public BufferedInputStream getDataStream() throws IOException {
        BufferedInputStream stream = new BufferedInputStream(getContent().getInputStream());
        stream.skip(dataOffset);
        return stream;
    }

    /**
     * @return the content
     */
    private synchronized RandomAccessContent getContent() {
        try {
            if (!getFile().getContent().isOpen()) {
                content = null;
            }
            if (content == null) {
                if (isReadOnly()) {
                    content = getFile().getContent().getRandomAccessContent(RandomAccessMode.READ);
                } else {
                    content = getFile().getContent().getRandomAccessContent(RandomAccessMode.READWRITE);
                }
            }
        } catch (IOException | ParseException ex) {
            throw new RuntimeException(ex);
        }
        return content;
    }

    /**
     * @return the file
     */
    private synchronized FileObject getFile() throws IOException, ParseException {
        if (this.file == null) {
            this.file = VFS.getManager().resolveFile(uri);
            try (InputStream stream = file.getContent().getInputStream()) {
                LoggerFactory.getLogger(getClass()).debug("Reading header of FileEnvelope " + uri);
                Envelope header = DefaultEnvelopeReader.instance.customRead(stream, null);
                this.properties = header.getProperties();
                meta = header.meta();
                dataOffset = Tag.TAG_LENGTH + header.getProperties().get(META_LENGTH_KEY).intValue();
                dataSize = Integer.toUnsignedLong(header.getProperties().get(DATA_LENGTH_KEY).intValue());
            }
        }
        return this.file;
    }
}

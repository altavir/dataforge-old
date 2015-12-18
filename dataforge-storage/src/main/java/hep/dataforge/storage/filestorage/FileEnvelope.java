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

import hep.dataforge.meta.Meta;
import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Tag;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * A specific envelope to handle file storage format.
 *
 * @author Alexander Nozik
 */
//TODO add reentrantlock to ensure read/write synchronization
public class FileEnvelope implements Envelope {

    public static final long INFINITE_DATA_SIZE = Integer.toUnsignedLong(-1);
    private static final long DATA_SIZE_PROPERTY_OFFSET = 22;

    private final boolean readOnly;
    private final FileObject file;
    private Meta meta;
    private final RandomAccessContent content;
    private Map<String, Value> properties;
    private long dataOffset;
    private long dataSize;

    public FileEnvelope(FileObject file, boolean readOnly) throws IOException, ParseException {
        this.file = file;
        readHeader(file);
        if (readOnly || !file.isWriteable()) {
            this.readOnly = true;
            content = file.getContent().getRandomAccessContent(RandomAccessMode.READ);
        } else {
            this.readOnly = false;
            content = file.getContent().getRandomAccessContent(RandomAccessMode.READWRITE);
        }
    }

//    public FileEnvelope(FileObject file) throws IOException, ParseException {
//        this(file, true);
//    }

    @Override
    public ByteBuffer getData() {
        try {
            if (dataSize == INFINITE_DATA_SIZE) {
                dataSize = content.length() - dataOffset;
            }

            content.seek(dataOffset);

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
        return properties;
    }

    public String readLine(int offset) throws IOException {
        content.seek(offset);
        return readLine();
    }

    public String readLine() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte nextChar = content.readByte();
        while (content.getFilePointer() < content.length() && nextChar != '\r') {
            buffer.put(nextChar);
            nextChar = content.readByte();

            if (!buffer.hasRemaining()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + 1024);
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
        }
        return new String(buffer.array(), Charset.forName("UTF-8"));
    }

    public ByteBuffer readBlock(int offset, int length) throws IOException {
        content.seek(offset);
        ByteBuffer block = ByteBuffer.allocate(length);
        while (block.hasRemaining()) {
            block.put(content.readByte());
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
    public ByteBuffer getHeader() throws IOException {
        return readBlock(0, (int) dataOffset);
    }

    /**
     * Create a new file with the same header, but without data.
     *
     * @throws IOException
     */
    public synchronized void clearData() throws IOException {
        ByteBuffer header = getHeader();
        try (OutputStream stream = file.getContent().getOutputStream(false)) {
            stream.write(header.array());
            setDataSize(0);
        }
    }

    public long readerPos() throws IOException {
        return content.getFilePointer();
    }

    /**
     * Reset file pointer to data start
     * @throws IOException 
     */
    public void resetPos() throws IOException {
        content.seek(dataOffset);
    }
    
    public void seek(long pos) throws IOException{
        content.seek(pos);
    }

    synchronized protected void setDataSize(int size) throws IOException {
        long offset = content.getFilePointer();
        content.seek(DATA_SIZE_PROPERTY_OFFSET);//seeking binary 
        content.writeInt(size); // write 4 bytes
        content.seek(offset);//return to the initial position
        properties.put(DATA_LENGTH_KEY, Value.of(size));//update property
    }

//    synchronized private void updateMetaSize() {
//        throw new UnsupportedOperationException();
//    }
    private void readHeader(FileObject file) throws IOException, ParseException {
        try (InputStream stream = file.getContent().getInputStream()) {
            Envelope header = DefaultEnvelopeReader.instance.customRead(stream, null);
            this.properties = header.getProperties();
            meta = header.meta();
            dataOffset = Tag.TAG_LENGTH + header.getProperties().get(META_LENGTH_KEY).intValue();
            dataSize = Integer.toUnsignedLong(header.getProperties().get(DATA_LENGTH_KEY).intValue());
        }
    }

    synchronized public void append(byte[] bytes) throws IOException {
        if (isReadOnly()) {
            throw new IOException("Trying to write to readonly file");
        } else {
            content.seek(eofPos());
            content.write(bytes);
            dataSize += bytes.length;
            setDataSize((int) (eofPos() - content.getFilePointer()));
        }

    }

    public long eofPos() throws IOException {
        return content.length();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

}

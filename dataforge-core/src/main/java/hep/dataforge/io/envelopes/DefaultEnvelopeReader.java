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
package hep.dataforge.io.envelopes;

import hep.dataforge.data.binary.Binary;
import hep.dataforge.data.binary.BufferedBinary;
import hep.dataforge.exceptions.EnvelopeFormatException;
import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.meta.Meta;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.function.Supplier;

import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;
import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;
import static java.nio.file.StandardOpenOption.READ;

/**
 * @author darksnake
 */
public class DefaultEnvelopeReader implements EnvelopeReader {

    public static final DefaultEnvelopeReader INSTANCE = new DefaultEnvelopeReader();


    protected EnvelopeTag newTag(){
        return new EnvelopeTag();
    }

    @Override
    public Envelope read(@NotNull InputStream stream) throws IOException {
        EnvelopeTag tag = newTag().read(stream);
        MetaStreamReader parser = tag.getMetaType().getReader();
        int metaLength = tag.getMetaSize();
        Meta meta;
        if (metaLength == 0) {
            meta = Meta.buildEmpty(DEFAULT_META_NAME);
        } else {
            try {
                meta = parser.read(stream, metaLength);
            } catch (ParseException ex) {
                throw new EnvelopeFormatException("Error parsing meta", ex);
            }
        }

        Binary binary;
        int dataLength = tag.getDataSize();
        //skipping separator for automatic meta reading
        if (metaLength == -1) {
            stream.skip(separator().length);
        }
        binary = readData(stream, dataLength);

        return new SimpleEnvelope(meta, binary);
    }

    /**
     * The envelope is lazy meaning it will be calculated on demand. If the
     * stream will be closed before that, than an error will be thrown. In order
     * to avoid this problem, it is wise to call {@code getData} after read.
     *
     * @return
     */
    public Envelope readLazy(Path path) throws IOException {
        SeekableByteChannel channel = Files.newByteChannel(path, READ);
        EnvelopeTag tag = newTag().read(channel);
        int metaLength = tag.getMetaSize();
        int dataLength = tag.getDataSize();
        if (metaLength < 0 || dataLength < 0) {
            LoggerFactory.getLogger(getClass()).error("Can't lazy read infinite data or meta. Returning non-lazy envelope");
            return read(path);
        }

        ByteBuffer metaBuffer = ByteBuffer.allocate(metaLength);
        channel.position(tag.getLength());
        channel.read(metaBuffer);
        MetaStreamReader parser = tag.getMetaType().getReader();

        Meta meta;
        if (metaLength == 0) {
            meta = Meta.buildEmpty(DEFAULT_META_NAME);
        } else {
            try {
                meta = parser.readBuffer(metaBuffer);
            } catch (ParseException ex) {
                throw new EnvelopeFormatException("Error parsing annotation", ex);
            }
        }
        channel.close();

        Supplier<Binary> supplier = () -> {
            try (SeekableByteChannel dataChannel = Files.newByteChannel(path, READ)) {
                dataChannel.position(tag.getLength() + metaLength);
                ByteBuffer dataBuffer = ByteBuffer.allocate(dataLength);
                dataChannel.read(dataBuffer);
                return new BufferedBinary(dataBuffer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };

        return new LazyEnvelope(meta, supplier);
    }

    protected byte[] separator() {
        return SEPARATOR;
    }

    public Binary readData(InputStream stream, int length) throws IOException {
        if (length == -1) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (stream.available() > 0) {
                baos.write(stream.read());
            }
            return new BufferedBinary(baos.toByteArray());
        } else {
            byte[] bytes = new byte[length];
            stream.read(bytes);
            return new BufferedBinary(bytes);
        }
    }

    /**
     * Read envelope with data (without lazy reading)
     *
     * @param stream
     * @return
     * @throws IOException
     */
    public Envelope readWithData(InputStream stream) throws IOException {
        Envelope res = read(stream);
        res.getData();
        return res;
    }

}

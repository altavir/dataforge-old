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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.function.Function;
import java.util.function.Supplier;

import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;

/**
 * @author darksnake
 */
public class DefaultEnvelopeReader implements EnvelopeReader {

    public static final DefaultEnvelopeReader INSTANCE = new DefaultEnvelopeReader();


    @Override
    public Envelope read(InputStream stream) throws IOException {
        return read(stream, EnvelopeTag::from);
    }

    /**
     * Read an envelope and override properties.
     * <p>
     * The envelope is lazy meaning it will be calculated on demand. If the
     * stream will be closed before that, than an error will be thrown. In order
     * to avoid this problem, it is wise to call {@code getData} after read.
     * </p>
     *
     * @param stream
     * @param tagReader
     * @return
     * @throws IOException
     */
    public Envelope read(@NotNull InputStream stream, @NotNull Function<InputStream,EnvelopeTag> tagReader) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(stream);
        EnvelopeTag tag = tagReader.apply(bis);
        MetaStreamReader parser = tag.getMetaType().getReader();
        int metaLength = tag.getMetaSize();
        Meta meta;
        if (metaLength == 0) {
            meta = Meta.buildEmpty("meta");
        } else {
            try {
                meta = parser.read(bis, metaLength);
            } catch (ParseException ex) {
                throw new EnvelopeFormatException("Error parsing annotation", ex);
            }
        }

        Supplier<Binary> supplier = () -> {
            int dataLength = tag.getDataSize();
            try {
                //skipping separator for automatic meta reading
                if (metaLength == -1) {
                    bis.skip(separator().length);
                }
                return readData(bis, dataLength);
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

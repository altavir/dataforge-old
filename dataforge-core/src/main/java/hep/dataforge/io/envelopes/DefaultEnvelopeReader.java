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
import hep.dataforge.values.Value;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hep.dataforge.io.envelopes.DefaultEnvelopeType.CUSTOM_PROPERTY_HEAD;
import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;
import static hep.dataforge.io.envelopes.Envelope.*;
import static hep.dataforge.io.envelopes.EnvelopePropertyCodes.getMetaType;

/**
 *
 * @author darksnake
 */
public class DefaultEnvelopeReader implements EnvelopeReader<Envelope> {

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final DefaultEnvelopeReader instance = new DefaultEnvelopeReader();

    public Map<String, Value> readProperties(BufferedInputStream stream) throws IOException {
        Map<String, Value> res = new HashMap<>();
        stream.mark(Tag.TAG_LENGTH);
        byte[] tagBuffer = new byte[Tag.TAG_LENGTH];
        stream.read(tagBuffer);
        if (Tag.isValidTag(tagBuffer)) {
            Tag tag = new Tag().read(tagBuffer);
            res.putAll(tag.asProperties());
        } else {
//            throw new IOException("Wrong envelope format.");
//            stream.reset();
            return null;
        }

        //reading custom properties
        stream.mark(DEFAULT_BUFFER_SIZE);
        byte[] smallBuffer = new byte[2];
        stream.read(smallBuffer);
        Pattern pattern = Pattern.compile("(?<key>[\\w\\.]*)\\s*\\:\\s*(?<value>[^;]*)");

        while (Arrays.equals(CUSTOM_PROPERTY_HEAD, smallBuffer)) {
            ByteBuffer customPropertyBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE - 2);
            byte b;
            do {
                b = (byte) stream.read();
                customPropertyBuffer.put(b);
                if (customPropertyBuffer.position() == customPropertyBuffer.limit()) {
                    throw new RuntimeException("Custom properties with length more than " + customPropertyBuffer.limit() + " are not supported");
                }
            } while ('\n' != b);
            String line = new String(customPropertyBuffer.array()).trim();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String key = matcher.group("key");
                String value = matcher.group("value");
                res.put(key, Value.of(value));
            } else {
                throw new RuntimeException("Custom property definition does not match format");
            }
            stream.mark(DEFAULT_BUFFER_SIZE);
            stream.read(smallBuffer);
        }
        stream.reset();

        return res;
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
     * @param overrideProperties properties to override. Ignored if null.
     * @return
     * @throws IOException
     */
    public Envelope customRead(InputStream stream, Map<String, Value> overrideProperties) throws IOException {
        BufferedInputStream bis;
        if (stream instanceof BufferedInputStream) {
            bis = (BufferedInputStream) stream;
        } else {
            bis = new BufferedInputStream(stream);
        }
        Map<String, Value> properties = readProperties(bis);

        if (properties == null) {
            throw new IOException("Empty envelope");
        }

        if (overrideProperties != null) {
            properties.putAll(overrideProperties);
        }

        MetaStreamReader parser = getMetaType(properties.get(META_TYPE_KEY)).getReader();
        Charset charset = Charset.forName(properties.get(META_ENCODING_KEY).stringValue());//EnvelopePropertyCodes.getCharset(newProperties.get(META_ENCODING_KEY));
        int metaLength = properties.get(META_LENGTH_KEY).intValue();
        Meta meta;
        if (metaLength == 0) {
            meta = Meta.buildEmpty("meta");
        } else {
            try {
                meta = parser.read(bis, metaLength, charset);
            } catch (ParseException ex) {
                throw new EnvelopeFormatException("Error parsing annotation", ex);
            }
        }

        Supplier<Binary> supplier = () -> {
            int dataLength = properties.get(DATA_LENGTH_KEY).intValue();
            try {
                if (metaLength == Tag.INFINITE_SIZE) {
                    bis.skip(separator().length);
                }
                return readData(bis, dataLength);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };

        return new LazyEnvelope(properties, meta, supplier);
    }

    public boolean isCorrectProtocolVersion(Map<String, Value> properties) {
        return !properties.containsKey(VERSION_KEY) || properties.get(VERSION_KEY).intValue() == Tag.CURRENT_PROTOCOL_VERSION;
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

    @Override
    public Envelope read(InputStream stream) throws IOException {
        return customRead(stream, null);
    }

    /**
     * Read envelope with data (without lazy reading)
     *
     * @param stream
     * @return
     * @throws IOException
     */
    public Envelope readWithData(InputStream stream) throws IOException {
        Envelope res = customRead(stream, null);
        res.getData();
        return res;
    }

}

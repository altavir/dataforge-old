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

import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.values.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static hep.dataforge.io.envelopes.Coder.getMetaType;
import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;
import static hep.dataforge.io.envelopes.Envelope.*;

/**
 *
 * @author darksnake
 */
public class DefaultEnvelopeWriter implements EnvelopeWriter<Envelope> {

    public static final DefaultEnvelopeWriter instance = new DefaultEnvelopeWriter();

    private static final Map<String, Value> defaultProperties;

    static {
        defaultProperties = new HashMap<>();
        defaultProperties.put(META_TYPE_KEY, Value.of(0));
        defaultProperties.put(META_ENCODING_KEY, Value.of(0));
    }

    @Override
    public void write(OutputStream stream, Envelope envelope) throws IOException {
        write(stream, envelope, true);
    }

    /**
     * Automatically define meta size and data size if it is not defined already
     * and write envelope to the stream
     *
     * @param stream
     * @param envelope
     * @param useStamp
     * @throws IOException
     */
    public void write(OutputStream stream, Envelope envelope, boolean useStamp) throws IOException {

        Map<String, Value> newProperties = new HashMap<>(defaultProperties);
        newProperties.putAll(envelope.getProperties());

        MetaStreamWriter writer = getMetaType(newProperties.get(META_TYPE_KEY)).getWriter();
        Charset charset = Charset.forName(newProperties.get(META_ENCODING_KEY).stringValue());//Coder.getCharset(newProperties.get(META_ENCODING_KEY));
        byte[] meta;
        int metaSize;
        if (envelope.meta().isEmpty()) {
            meta = new byte[0];
            metaSize = 0;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.write(baos, envelope.meta(), charset);
            meta = baos.toByteArray();
            metaSize = meta.length + 2;
        }
        newProperties.putIfAbsent(META_LENGTH_KEY, Value.of(metaSize));

//        byte[] dataBytes = envelope.getData().array();
        long dataSize = envelope.getData().size();
        newProperties.putIfAbsent(DATA_LENGTH_KEY, Value.of(dataSize));

        if (useStamp) {
            Tag stamp = new Tag();
            newProperties = stamp.applyProperties(newProperties);
            stream.write(stamp.asByteArray());
        }

        for (Map.Entry<String, Value> entry : newProperties.entrySet()) {
            stream.write(String.format("#? %s: %s", entry.getKey(), entry.getValue().stringValue()).getBytes());
            stream.write(SEPARATOR);
        }

        stream.write(meta);
        if (meta.length > 0) {
            stream.write(SEPARATOR);
        }

        if (dataSize > 0) {
            InputStream inputStream = envelope.getData().getStream();
            for (int i = 0; i < dataSize; i++) {
                stream.write(inputStream.read());
            }
        } else if (dataSize < 0) {
            InputStream inputStream = envelope.getData().getStream();
            while (inputStream.available() > 0) {
                stream.write(inputStream.read());
            }
        }

    }

}

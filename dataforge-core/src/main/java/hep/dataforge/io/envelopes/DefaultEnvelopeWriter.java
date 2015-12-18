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
import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.values.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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

        MetaStreamWriter writer = MetaWriterLibrary.instance().get(newProperties.get(META_TYPE_KEY));
        Charset charset = CharsetLibrary.instance().get(newProperties.get(META_ENCODING_KEY));
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

        byte[] dataBytes = envelope.getData().array();
        int dataSize = dataBytes.length;
        newProperties.putIfAbsent(DATA_LENGTH_KEY, Value.of(dataSize));

        if (useStamp) {
            Tag stamp = Tag.fromProperties(newProperties);
            stream.write(stamp.asByteArray());
            //Removeing keys that are already in the stamp
            newProperties.remove(DATA_LENGTH_KEY);
            newProperties.remove(DATA_TYPE_KEY);
            newProperties.remove(META_ENCODING_KEY);
            newProperties.remove(META_LENGTH_KEY);
            newProperties.remove(META_TYPE_KEY);
            newProperties.remove(OPT_KEY);
            newProperties.remove(TYPE_KEY);
            newProperties.remove(VERSION_KEY);
        }

        for (Map.Entry<String, Value> entry : newProperties.entrySet()) {
            stream.write(String.format("#? %s: %s ;", entry.getKey(), entry.getValue().stringValue()).getBytes());
            stream.write(SEPARATOR);
        }

        stream.write(meta);
        if (meta.length > 0) {
            stream.write(SEPARATOR);
        }
        stream.write(dataBytes);
    }

}

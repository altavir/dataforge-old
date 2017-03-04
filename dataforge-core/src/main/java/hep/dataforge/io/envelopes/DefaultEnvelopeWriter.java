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
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static hep.dataforge.io.envelopes.DefaultEnvelopeType.SEPARATOR;
import static hep.dataforge.io.envelopes.Envelope.DATA_LENGTH_KEY;
import static hep.dataforge.io.envelopes.Envelope.META_LENGTH_KEY;

/**
 * @author darksnake
 */
public class DefaultEnvelopeWriter implements EnvelopeWriter {
    private static final Set<String> TAG_PROPERTIES = new HashSet<>(
            Arrays.asList(new String[]{Envelope.TYPE_KEY, Envelope.META_TYPE_KEY, Envelope.META_LENGTH_KEY, Envelope.DATA_LENGTH_KEY})
    );

    public static final DefaultEnvelopeWriter instance = new DefaultEnvelopeWriter();

    private MetaType metaType = XMLMetaType.instance;

    public DefaultEnvelopeWriter withMetaType(@NotNull MetaType metaType) {
        DefaultEnvelopeWriter newInstance = new DefaultEnvelopeWriter();
        newInstance.metaType = metaType;
        return newInstance;
    }

    @Override
    public void write(OutputStream stream, Envelope envelope) throws IOException {
        EnvelopeTag tag = new EnvelopeTag().setMetaType(metaType);
        write(stream, tag, envelope);
    }

    /**
     * Automatically define meta size and data size if it is not defined already
     * and write envelope to the stream
     *
     * @param stream
     * @param envelope
     * @throws IOException
     */
    protected final void write(OutputStream stream, EnvelopeTag tag, Envelope envelope) throws IOException {

        MetaStreamWriter writer = tag.getMetaType().getWriter();
        byte[] meta;
        int metaSize;
        if (envelope.meta().isEmpty()) {
            meta = new byte[0];
            metaSize = 0;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.write(baos, envelope.meta());
            meta = baos.toByteArray();
            metaSize = meta.length + 2;
        }
        tag.setValue(META_LENGTH_KEY, metaSize);

//        byte[] dataBytes = envelope.getData().array();
        long dataSize = envelope.getData().size();
        tag.setValue(DATA_LENGTH_KEY, dataSize);

        stream.write(tag.byteHeader());

        for (Map.Entry<String, Value> entry : tag.getValues().entrySet()) {
            if (TAG_PROPERTIES.contains(entry.getKey())) {
            } else {
                stream.write(String.format("#? %s: %s", entry.getKey(), entry.getValue().stringValue()).getBytes());
                stream.write(SEPARATOR);
            }
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

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
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * The convenient builder for envelopes
 *
 * @author Alexander Nozik
 */
public class EnvelopeBuilder implements Envelope {

    private MetaBuilder meta = new MetaBuilder("envelope");

    //initializing with empty buffer
    private Binary data = new BufferedBinary(new byte[0]);

    public EnvelopeBuilder(Envelope envelope) {
        this.meta = envelope.meta().getBuilder();
        this.data = envelope.getData();
    }

    public EnvelopeBuilder() {

    }

    public EnvelopeBuilder setMeta(Meta annotation) {
        this.meta = annotation.getBuilder();
        return this;
    }

    /**
     * Get modifiable meta builder for this envelope
     *
     * @return
     */
    public MetaBuilder getMetaBuilder() {
        return this.meta;
    }

    /**
     * Helper to fast put node to envelope meta
     *
     * @param element
     * @return
     */
    public EnvelopeBuilder putMetaNode(String nodeName, Meta element) {
        this.meta.putNode(nodeName, element);
        return this;
    }

    public EnvelopeBuilder putMetaNode(Meta element) {
        this.meta.putNode(element);
        return this;
    }

    /**
     * Helper to fast put value to meta root
     *
     * @param name
     * @param value
     * @return
     */
    public EnvelopeBuilder putMetaValue(String name, Object value) {
        this.meta.putValue(name, value);
        return this;
    }

    public EnvelopeBuilder setData(Binary data) {
        this.data = data;
        return this;
    }

    public EnvelopeBuilder setData(ByteBuffer data) {
        this.data = new BufferedBinary(data);
        return this;
    }

    public EnvelopeBuilder setData(byte[] data) {
        this.data = new BufferedBinary(data);
        return this;
    }

    public EnvelopeBuilder setData(Consumer<OutputStream> data) {
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        data.accept(baos);
        return setData(baos.toByteArray());
    }

    public EnvelopeBuilder setContentType(String type){
        putMetaValue("@envelope.type",type);
        return this;
    }

    public EnvelopeBuilder setContentDescription(String description){
        putMetaValue("@envelope.description",description);
        return this;
    }

    public Meta getMeta() {
        return meta;
    }

    @Override
    public Meta meta() {
        return meta;
    }

    public Binary getData() {
        return data;
    }

    public Envelope build() {
        return new SimpleEnvelope(meta, data);
    }

    @NotNull
    private Object writeReplace() throws ObjectStreamException {
        return new SimpleEnvelope(meta(), getData());
    }
}

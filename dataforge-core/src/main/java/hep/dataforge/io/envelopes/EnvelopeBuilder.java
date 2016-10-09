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
import hep.dataforge.values.Value;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static hep.dataforge.io.envelopes.Envelope.*;

/**
 * The convenient build for envelopes
 *
 * @author Alexander Nozik
 */
public class EnvelopeBuilder {

    private Map<String, Value> properties = new HashMap<>();
    private MetaBuilder meta = new MetaBuilder("envelope");

    //initializing with empty buffer
    private Binary data = new BufferedBinary(new byte[0]);

    public EnvelopeBuilder(Envelope envelope) {
        this.properties = envelope.getProperties();
        this.meta = envelope.meta().getBuilder();
        this.data = envelope.getData();
    }

    public EnvelopeBuilder() {
        this.properties.put(VERSION_KEY, Value.of(Tag.CURRENT_PROTOCOL_VERSION));
    }

    public EnvelopeBuilder setProperty(String name, Value value) {
        properties.put(name, value);
        return this;
    }

    public EnvelopeBuilder setProperty(String name, String value) {
        return setProperty(name, Value.of(value));
    }

    public EnvelopeBuilder setProperty(String name, Number value) {
        return setProperty(name, Value.of(value));
    }

    public EnvelopeBuilder setProperties(Map<String, Value> properties) {
        for (Map.Entry<String, Value> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
        return this;
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

    public EnvelopeBuilder setMetaType(String metaType) {
        return setProperty(META_TYPE_KEY, metaType);
    }

    public EnvelopeBuilder setMetaEncoding(String metaEncoding) {
        return setProperty(META_ENCODING_KEY, metaEncoding);
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

    //    public EnvelopeBuilder setPriority(int priority) {
//        return setProperty(MESSAGE_PRIORITY_KEY, priority);
//    }
    public EnvelopeBuilder setEnvelopeType(EnvelopeType type) {
        return this.setProperty(TYPE_KEY, type.getName());
    }

    public EnvelopeBuilder setEnvelopeType(String type) {
        return this.setProperty(TYPE_KEY, type);
    }

    public EnvelopeBuilder setEnvelopeType(short code) {
        Value type = Coder.decode(TYPE_KEY, code);
        return this.setProperty(TYPE_KEY, type);
    }

    public EnvelopeBuilder setDataType(String type) {
        return this.setProperty(DATA_TYPE_KEY, type);
    }

    public EnvelopeBuilder setDataType(int code) {
        Value type = Coder.decode(DATA_TYPE_KEY, code);
        return this.setProperty(DATA_TYPE_KEY, type);
    }

    //    public EnvelopeBuilder setTime(LocalDateTime time){
//        return this.setProperty(OPT_KEY, time.toEpochSecond(ZoneOffset.UTC));
//    }
    public EnvelopeBuilder setInfiniteDataSize() {
        return setProperty(DATA_LENGTH_KEY, -1);
    }

    public EnvelopeBuilder setInfiniteMetaSize() {
        return setProperty(META_LENGTH_KEY, -1);
    }

    public Map<String, Value> getProperties() {
        return properties;
    }

    public Meta getMeta() {
        return meta;
    }

    public Binary getData() {
        return data;
    }

    public Envelope build() {
        return new SimpleEnvelope(properties, meta, data);
    }
}

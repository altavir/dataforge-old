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

import hep.dataforge.values.CompositePropertyValue;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static hep.dataforge.io.envelopes.Envelope.*;
import static hep.dataforge.io.envelopes.EnvelopeProperties.ASCII_CHARSET;

/**
 * An Envelope critical properties that could be encoded in a short binary
 * representation.
 *
 * @author Alexander Nozik
 */
public class Tag {

    public static final short CURRENT_PROTOCOL_VERSION = 0x3031;

    public static final short MAX_SHORT = -1;

    public static final int MAX_INT = -1;

    public static final int INFINITE_SIZE = MAX_INT;

    /**
     * The length of serialization tag in bytes
     */
    public static final int TAG_LENGTH = 30;

    public static byte[] START_SEQUENCE = {'#', '!'};

    public static byte[] END_SEQUENCE = {'!', '#', '\r', '\n'};

    /**
     * Create serialization tag from String enclosed in #!...!# with or without
     * new line (trim is used).
     *
     * @param str
     * @return
     */
    public static Tag fromString(String str) {
        return new Tag().read(str.trim().getBytes(ASCII_CHARSET));
    }

    public static boolean isValidTag(byte[] bytes) {
        return (bytes[0] == START_SEQUENCE[0])
                && (bytes[1] == START_SEQUENCE[1])
                && (bytes[26] == END_SEQUENCE[0])
                && (bytes[27] == END_SEQUENCE[1]);
    }

    public short protocolVersion = CURRENT_PROTOCOL_VERSION;

    public short type = 0;

    public long opt = 0;

    public short metaType = 0x0000;

    public short metaEncoding = 0x0000;

    public long metaLength = -1;

    public int dataType = 0;

    public long dataLength = -1;

    /**
     * Read binary string into this tag
     *
     * @param bytes
     * @return
     */
    public Tag read(byte[] bytes) {

        if (isValidTag(bytes)) {

            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            this.protocolVersion = buffer.getShort(2);
            this.type = buffer.getShort(4);

            this.opt = Integer.toUnsignedLong(buffer.getInt(6));

            this.metaType = buffer.getShort(10);
            this.metaEncoding = buffer.getShort(12);

            this.metaLength = Integer.toUnsignedLong(buffer.getInt(14));

            this.dataType = buffer.getInt(18);

            this.dataLength = Integer.toUnsignedLong(buffer.getInt(22));
            return this;
        } else {
            throw new IllegalArgumentException("Wrong format of tag line");
        }
    }

    /**
     * Apply all valid properties from given map. Return a copy containing all remaining properties
     * @param map
     * @return 
     */
    public Map<String, Value> applyProperties(Map<String, Value> map) {
        Map<String,Value> properties = new HashMap<>(map);
        protocolVersion = (short) (properties.containsKey(VERSION_KEY) ? properties.get(VERSION_KEY).intValue() : CURRENT_PROTOCOL_VERSION);
        properties.remove(VERSION_KEY);
        type = getShortCompositePropertyValue(properties, TYPE_KEY);
        opt = properties.containsKey(OPT_KEY) ? properties.get(OPT_KEY).intValue() : 0;
        properties.remove(OPT_KEY);
        metaType = getShortCompositePropertyValue(properties, META_TYPE_KEY);
        metaEncoding = getShortCompositePropertyValue(properties, META_ENCODING_KEY);
        metaLength = (properties.containsKey(META_LENGTH_KEY) ? properties.get(META_LENGTH_KEY).intValue() : -1);
        properties.remove(META_LENGTH_KEY);
        dataType = getCompositePropertyValue(properties, DATA_TYPE_KEY);
        dataLength = (properties.containsKey(DATA_LENGTH_KEY) ? properties.get(DATA_LENGTH_KEY).numberValue().longValue() : -1);
        properties.remove(DATA_LENGTH_KEY);
        return properties;
    }
    

    private int getCompositePropertyValue(Map<String, Value> properties, String key) {
        if (properties.containsKey(key)) {
            Value val = properties.get(key);
            if (val.valueType() == ValueType.NUMBER || val instanceof CompositePropertyValue) {
                properties.remove(key);
                return val.intValue();
            } else {
                // place ? int tag and use custom property instead
                return 0x3f3f3f3f;
            }
        } else {
            return 0;
        }
    }

    private short getShortCompositePropertyValue(Map<String, Value> properties, String key) {
        if (properties.containsKey(key)) {
            Value val = properties.get(key);
            if (val.valueType() == ValueType.NUMBER || val instanceof CompositePropertyValue) {
                properties.remove(key);
                return (short) val.intValue();
            } else {
                // place ? int tag and use custom property instead
                return 0x3f3f;
            }
        } else {
            return 0;
        }
    }    

    /**
     * Build new serialization tag string with enclosing #! and !# but without
     * new line
     *
     * @return
     */
    @Override
    public String toString() {
        return new String(asByteArray(), ASCII_CHARSET);
    }

    public byte[] asByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(30);
        buffer.put(START_SEQUENCE);

        buffer.putShort(protocolVersion);
        buffer.putShort(type);

        buffer.putInt(new Long(opt).intValue());

        buffer.putShort(metaType);
        buffer.putShort(metaEncoding);

        buffer.putInt(new Long(metaLength).intValue());

        buffer.putInt(dataType);

        buffer.putInt(new Long(dataLength).intValue());

        buffer.put(END_SEQUENCE);
        return buffer.array();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.protocolVersion;
        hash = 19 * hash + this.type;
        hash = 19 * hash + (int) (this.opt ^ (this.opt >>> 32));
        hash = 19 * hash + this.metaType;
        hash = 19 * hash + this.metaEncoding;
        hash = 19 * hash + (int) (this.metaLength ^ (this.metaLength >>> 32));
        hash = 19 * hash + this.dataType;
        hash = 19 * hash + (int) (this.dataLength ^ (this.dataLength >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tag other = (Tag) obj;
        if (this.protocolVersion != other.protocolVersion) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.opt != other.opt) {
            return false;
        }
        if (this.metaType != other.metaType) {
            return false;
        }
        if (this.metaEncoding != other.metaEncoding) {
            return false;
        }
        if (this.metaLength != other.metaLength) {
            return false;
        }
        if (this.dataType != other.dataType) {
            return false;
        }
        return this.dataLength == other.dataLength;
    }

    public Map<String, Value> asProperties() {
        Map<String, Value> res = new HashMap<>();
        res.put(TYPE_KEY, Value.of(type));
        res.put(OPT_KEY, Value.of(Instant.ofEpochMilli(opt)));
        res.put(META_ENCODING_KEY, EnvelopeProperties.getCharsetValue(metaEncoding));
        res.put(META_TYPE_KEY, EnvelopeProperties.getMetaType(metaType).getValue());
        res.put(META_LENGTH_KEY, Value.of(metaLength));
        res.put(DATA_TYPE_KEY, Value.of(dataType));
        res.put(DATA_LENGTH_KEY, Value.of(dataLength));

        return res;
    }

}

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

import static hep.dataforge.io.envelopes.CharsetLibrary.ASCII_CHARSET;
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.values.Value;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * An Envelope critical properties that could be encoded in a short binary representation.
 *
 * @author Alexander Nozik
 */
public class Tag {

    public static final short CURRENT_PROTOCOL_VERSION = 0x0001;
    
    public static final short MAX_SHORT = -1;

    public static final int MAX_INT = -1;

    public static final int INFINITE_SIZE = MAX_INT;
    

    /**
     * The length of serialization tag in bytes
     */
    public static final int TAG_LENGTH = 30;

    public static byte[] START_SEQUENCE = {'#', '!'};

    public static byte[] END_SEQUENCE = {'!', '#','\r','\n'};

    /**
     * Create serialization tag from String enclosed in #!...!# with or without
     * new line (trim is used).
     *
     * @param str
     * @return 
     */
    public static Tag fromString(String str) {
        str = str.trim();
        return new Tag(str.getBytes(ASCII_CHARSET));
    }

    /**
     * Create Tag from propertiSet using defaults for not presented properties
     * @param properties
     * @return 
     */
    public static Tag fromProperties(Map<String, Value> properties) {
        int protocolVersion = CURRENT_PROTOCOL_VERSION;
        int type = (properties.containsKey(TYPE_KEY) ? properties.get(TYPE_KEY).intValue() : 0);
        int time = properties.containsKey(OPT_KEY) ? properties.get(OPT_KEY).intValue(): 0;
        int metaType = (properties.containsKey(META_TYPE_KEY) ? properties.get(META_TYPE_KEY).intValue() : 0);
        int metaEncoding = (properties.containsKey(META_ENCODING_KEY) ? properties.get(META_ENCODING_KEY).intValue() : 0);
        long metaLength = (properties.containsKey(META_LENGTH_KEY) ? properties.get(META_LENGTH_KEY).intValue() : -1);
        int dataType = (properties.containsKey(DATA_TYPE_KEY) ? properties.get(DATA_TYPE_KEY).intValue() : 0);
        long dataLength = (properties.containsKey(DATA_LENGTH_KEY) ? properties.get(DATA_LENGTH_KEY).numberValue().longValue() : -1);
        return new Tag(protocolVersion, type, time, metaType, metaEncoding, metaLength, dataType, dataLength);
    }

    public static boolean isValidTag(byte[] bytes) {
        return (bytes[0] == START_SEQUENCE[0])
                && (bytes[1] == START_SEQUENCE[1])
                && (bytes[26] == END_SEQUENCE[0])
                && (bytes[27] == END_SEQUENCE[1]);
    }

    private short protocolVersion = CURRENT_PROTOCOL_VERSION;

    private final short type;

    private final long time;

    private short metaType = 0x0000;

    private short metaEncoding = 0x0000;

    private final long metaLength;

    private final int dataType;

    private final long dataLength;

    public Tag(int protocolVersion, int type, int time, int metaType, int metaEncoding, long metaLength, int dataType, long dataLength) {
        //TODO do runtime oveflow checks
        this.protocolVersion = (short) protocolVersion;
        this.type = (short) type;
        this.time = time;
        this.metaType = (short) metaType;
        this.metaEncoding = (short) metaEncoding;
        this.metaLength = metaLength;
        this.dataType = dataType;
        this.dataLength = dataLength;
    }

    public Tag(short type, short metaType, long metaLength, int dataType, long dataLength) {
        this.type = type;
        this.time = Instant.now().getEpochSecond();
        this.metaType = metaType;
        this.metaLength = metaLength;
        this.dataType = dataType;
        this.dataLength = dataLength;
    }

    public Tag(short type, int dataType, long dataLength) {
        this.type = type;
        this.time = Instant.now().getEpochSecond();
        this.metaLength = 0;
        this.dataType = dataType;
        this.dataLength = dataLength;
    }

    public Tag(short type, short metaType, long metaLength) {
        this.type = type;
        this.time = Instant.now().getEpochSecond();
        this.metaType = metaType;
        this.metaLength = metaLength;
        this.dataType = 0;
        this.dataLength = 0;
    }

    public Tag(byte[] bytes) {

        if (isValidTag(bytes)) {

            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            this.protocolVersion = buffer.getShort(2);
            this.type = buffer.getShort(4);

            this.time = Integer.toUnsignedLong(buffer.getInt(6));

            this.metaType = buffer.getShort(10);
            this.metaEncoding = buffer.getShort(12);

            this.metaLength = Integer.toUnsignedLong(buffer.getInt(14));

            this.dataType = buffer.getInt(18);

            this.dataLength = Integer.toUnsignedLong(buffer.getInt(22));
        } else {
            throw new IllegalArgumentException("Wrong format of tag line");
        }
    }

    /**
     * Get the name of dataLength
     *
     * @return the name of dataLength
     */
    public long getDataLength() {
        return dataLength;
    }

    /**
     * Get the name of dataType
     *
     * @return the name of dataType
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Get the name of metaLength
     *
     * @return the name of metaLength
     */
    public long getMetaLength() {
        return metaLength;
    }

    /**
     * Get the name of metaEncoding
     *
     * @return the name of metaEncoding
     */
    public short getMetaEncoding() {
        return metaEncoding;
    }

    /**
     * Get the name of metaType
     *
     * @return the name of metaType
     */
    public short getMetaType() {
        return metaType;
    }

    /**
     * Get the name of time
     *
     * @return the name of time
     */
    public Instant getTime() {
        return Instant.ofEpochSecond(time);
    }

    /**
     * Get the name of type
     *
     * @return the name of type
     */
    public short getType() {
        return type;
    }

    /**
     * Get the name of protocolVersion
     *
     * @return the name of protocolVersion
     */
    public short getProtocolVersion() {
        return protocolVersion;
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

        buffer.putInt(new Long(time).intValue());

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
        hash = 19 * hash + (int) (this.time ^ (this.time >>> 32));
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
        if (this.time != other.time) {
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
        res.put(OPT_KEY, Value.of(Instant.ofEpochMilli(time)));
        res.put(META_ENCODING_KEY, CharsetLibrary.instance().findValue(metaEncoding));
        res.put(META_TYPE_KEY, MetaReaderLibrary.instance().findValue(metaType));
        res.put(META_LENGTH_KEY, Value.of(metaLength));
        res.put(DATA_TYPE_KEY, Value.of(dataType));
        res.put(DATA_LENGTH_KEY, Value.of(dataLength));

        return res;
    }

}

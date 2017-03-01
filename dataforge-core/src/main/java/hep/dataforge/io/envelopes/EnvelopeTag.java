package hep.dataforge.io.envelopes;

import hep.dataforge.values.NamedValue;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Envelope tag converter v2
 * Created by darksnake on 25-Feb-17.
 */
public class EnvelopeTag {

    public static final byte[] START_SEQUENCE = {'#', '~'};
    public static final byte[] END_SEQUENCE = {'~', '#', '\r', '\n'};
    public static final String CUSTOM_PROPERTY_HEAD = "#?";

    public static byte[] LEGACY_START_SEQUENCE = {'#', '!'};

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Map<String, Value> values = new HashMap<>();
    private MetaType metaType = XMLMetaType.instance;
    private EnvelopeType envelopeType = DefaultEnvelopeType.instance;
    private int length = -1;

    /**
     * Get the length of tag in bytes. -1 means undefined size in case tag was modified
     *
     * @return
     */
    public int getLength() {
        return length;
    }

    private Map<String, Value> readLegacyHeader(InputStream stream) throws IOException {
        length += 28;
        byte[] bytes = new byte[28];
        stream.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Map<String, Value> res = new HashMap<>();

        int type = buffer.getInt();
        res.put(Envelope.TYPE_KEY, Value.of(type));

        short metaTypeCode = buffer.getShort(8);
        MetaType metaType = MetaType.resolve(metaTypeCode);

        if (metaType != null) {
            res.put(Envelope.META_TYPE_KEY, Value.of(metaType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve meta type. Using default");
        }

        long metaLength = Integer.toUnsignedLong(buffer.getInt(12));
        res.put(Envelope.META_LENGTH_KEY, Value.of(metaLength));
        long dataLength = Integer.toUnsignedLong(buffer.getInt(20));
        res.put(Envelope.DATA_LENGTH_KEY, Value.of(dataLength));
        return res;
    }

    /**
     * Read header line only
     *
     * @param stream
     * @throws IOException
     */
    private Map<String, Value> readHeader(InputStream stream) throws IOException {
        length += 18;

        Map<String, Value> res = new HashMap<>();

        byte[] body = new byte[14];
        stream.read(body);

        ByteBuffer buffer = ByteBuffer.wrap(body).order(ByteOrder.BIG_ENDIAN);

        //reading type
        int type = buffer.getInt();
        EnvelopeType envelopeType = EnvelopeType.resolve(type);

        if (envelopeType != null) {
            res.put(Envelope.TYPE_KEY, Value.of(envelopeType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve envelope type code. Using default");
        }

        //reading meta type
        short metaTypeCode = buffer.getShort(4);
        MetaType metaType = MetaType.resolve(metaTypeCode);

        if (metaType != null) {
            res.put(Envelope.META_TYPE_KEY, Value.of(metaType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve meta type. Using default");
        }

        //reading meta length
        long metaLength = Integer.toUnsignedLong(buffer.getInt(6));
        res.put(Envelope.META_LENGTH_KEY, Value.of(metaLength));
        //reading data length
        long dataLength = Integer.toUnsignedLong(buffer.getInt(10));
        res.put(Envelope.DATA_LENGTH_KEY, Value.of(dataLength));

        byte[] endSequence = new byte[4];
        stream.read(endSequence);

        if (!Arrays.equals(endSequence, END_SEQUENCE)) {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Wrong ending sequence for envelope tag");
        }
        return res;
    }

    /**
     * Read single custom line. Or reset stream position if line is not a property line
     *
     * @return true if line is s
     */
    @Nullable
    private NamedValue readCustomProperty(InputStream stream) throws IOException {
        stream.mark(DEFAULT_BUFFER_SIZE);
        byte[] smallBuffer = new byte[2];
        stream.read(smallBuffer);

        if (Arrays.equals(smallBuffer, CUSTOM_PROPERTY_HEAD.getBytes())) {
            length += 2;
            //reading line
            ByteBuffer customPropertyBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE - 2);
            byte b;
            do {
                b = (byte) stream.read();
                customPropertyBuffer.put(b);
                if (customPropertyBuffer.position() == customPropertyBuffer.limit()) {
                    throw new RuntimeException("Custom properties with length more than " + customPropertyBuffer.limit() + " are not supported");
                }
            } while ('\n' != b);
            length += customPropertyBuffer.position();
            String line = new String(customPropertyBuffer.array()).trim();

            Pattern pattern = Pattern.compile("(?<key>[\\w\\.]*)\\s*\\:\\s*(?<value>[^;]*)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String key = matcher.group("key");
                String value = matcher.group("value");
                return NamedValue.of(key, value);
            } else {
                throw new RuntimeException("Custom property definition does not match format");
            }
        } else {
            stream.reset();
            return null;
        }
    }

    public static EnvelopeTag from(InputStream stream) {
        try {
            return new EnvelopeTag().read(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read envelope tag", e);
        }
    }

//    public static EnvelopeTag fromLegacy(InputStream stream) throws IOException {
//        EnvelopeTag tag =  new EnvelopeTag();
//        tag.setValues(readLegacyHeader(stream));
//        //custom properties
//        NamedValue value;
//        do {
//            value = readCustomProperty(stream);
//            tag.setValue(value.getName(), value);
//        } while (value != null);
//        return tag;
//    }

    /**
     * Convert tag to properties
     *
     * @return
     */
    public Map<String, Value> getValues() {
        return values;
    }

    /**
     * Update existing properties
     *
     * @param props
     * @return
     */
    public void setValues(Map<String, Value> props) {
        props.forEach((key, value) -> setValue(key, value));
    }

    public void setValue(String name, Object value) {
        setValue(name, Value.of(value));
    }

    public void setValue(String name, Value value) {
        if (Envelope.TYPE_KEY.equals(name)) {
            EnvelopeType type = value.valueType() == ValueType.NUMBER ? EnvelopeType.resolve(value.intValue()) : EnvelopeType.resolve(value.stringValue());
            if (type != null) {
                envelopeType = type;
            } else {
                LoggerFactory.getLogger(getClass()).debug("Can't resolve envelope type");
            }
        } else if (Envelope.META_TYPE_KEY.equals(name)) {
            MetaType type = value.valueType() == ValueType.NUMBER ? MetaType.resolve((short) value.intValue()) : MetaType.resolve(value.stringValue());
            if (type != null) {
                metaType = type;
            } else {
                LoggerFactory.getLogger(getClass()).error("Can't resolve meta type");
            }
        } else {
            values.put(name, value);
        }
    }

    public long getMetaSize() {
        return values.getOrDefault(Envelope.META_LENGTH_KEY, Value.of(0)).longValue();
    }

    public long getDataSize() {
        return values.getOrDefault(Envelope.DATA_LENGTH_KEY, Value.of(0)).longValue();
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public EnvelopeType getEnvelopeType() {
        return envelopeType;
    }

    public EnvelopeTag read(InputStream stream) throws IOException {

        length = 2;

        Map<String, Value> header;
        stream.mark(30);

        //reading start sequence
        byte[] startSequence = new byte[2];
        stream.read(startSequence);

        if (Arrays.equals(startSequence, LEGACY_START_SEQUENCE)) {
            header = readLegacyHeader(stream);
        } else if (Arrays.equals(startSequence, START_SEQUENCE)) {
            header = readHeader(stream);
        } else if (Arrays.equals(startSequence, CUSTOM_PROPERTY_HEAD.getBytes())) {
            //No tag
            header = Collections.emptyMap();
            stream.reset();
            length = 0;
        } else {
            throw new IOException("Wrong start sequence for envelope tag");
        }

        setValues(header);
        //custom properties
        NamedValue value;
        do {
            value = readCustomProperty(stream);
            if (value != null) {
                setValue(value.getName(), value);
            }
        } while (value != null);
        return this;
    }

    public byte[] byteHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put(START_SEQUENCE);

        buffer.putInt(envelopeType.getCode());
        buffer.putShort(metaType.getCode());
        buffer.putInt((int) values.get(Envelope.META_LENGTH_KEY).longValue());
        buffer.putInt((int) values.get(Envelope.DATA_LENGTH_KEY).longValue());
        buffer.put(END_SEQUENCE);
        return buffer.array();
    }


}

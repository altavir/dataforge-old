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

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Map<String, Value> values = new HashMap<>();
    private MetaType metaType = XMLMetaType.instance;
    private EnvelopeType envelopeType = DefaultEnvelopeType.instance;

    /**
     * Read header line only
     *
     * @param stream
     * @throws IOException
     */
    public static Map<String, Value> readHeader(InputStream stream) throws IOException {
        //TODO add support for legacy envelopes
        Map<String, Value> res = new HashMap<>();

        //reading start sequence
        byte[] startSequence = new byte[2];
        stream.read(startSequence);
        if (!Arrays.equals(startSequence, START_SEQUENCE)) {
            throw new IOException("Wrong start sequence for envelope tag");
        }
        byte[] body = new byte[14];
        stream.read(body);

        ByteBuffer buffer = ByteBuffer.wrap(body).order(ByteOrder.BIG_ENDIAN);

        //reading type
        int type = buffer.getInt();
        res.put(Envelope.TYPE_KEY, Value.of(type));
        //reading meta type
        short metaTypeCode = buffer.getShort(4);
        MetaType metaType = MetaType.resolve(metaTypeCode);
        if (metaType != null) {
            res.put(Envelope.META_TYPE_KEY, Value.of(metaType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve meta type code. Using default");
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
    private static NamedValue readCustomProperty(InputStream stream) throws IOException {
        stream.mark(DEFAULT_BUFFER_SIZE);
        byte[] smallBuffer = new byte[2];
        stream.read(smallBuffer);

        if (Arrays.equals(smallBuffer, CUSTOM_PROPERTY_HEAD.getBytes())) {
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
                LoggerFactory.getLogger(getClass()).error("Can't resolve envelope type");
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
        setValues(readHeader(stream));
        //custom properties
        NamedValue value;
        do {
            value = readCustomProperty(stream);
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

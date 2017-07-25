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
import java.nio.channels.SeekableByteChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hep.dataforge.utils.Misc.UTF;

/**
 * Envelope tag converter v2
 * Created by darksnake on 25-Feb-17.
 */
public class EnvelopeTag {

    private static final byte[] START_SEQUENCE = {'#', '~'};
    private static final byte[] END_SEQUENCE = {'~', '#', '\r', '\n'};
    private static final String CUSTOM_PROPERTY_HEAD = "#?";
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static byte[] LEGACY_START_SEQUENCE = {'#', '!'};

    public static EnvelopeTag from(InputStream stream) {
        try {
            return new EnvelopeTag().read(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read envelope tag", e);
        }
    }

    public static EnvelopeTag from(SeekableByteChannel channel) {
        try {
            return new EnvelopeTag().read(channel);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read envelope tag", e);
        }
    }

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

    /**
     * Read leagscy version 1 tag without leading tag head
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    private Map<String, Value> readLegacyHeader(ByteBuffer buffer) throws IOException {
        Map<String, Value> res = new HashMap<>();

        int type = buffer.getInt(0);
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
     * Read header line only without leading two symbols
     *
     * @throws IOException
     */
    private Map<String, Value> readHeader(ByteBuffer buffer) throws IOException {

        Map<String, Value> res = new HashMap<>();

        //reading type
        int type = buffer.getInt(0);
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
        buffer.position(14);
        buffer.get(endSequence);

        if (!Arrays.equals(endSequence, END_SEQUENCE)) {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Wrong ending sequence for envelope tag");
        }
        return res;
    }

    private NamedValue getCustomProperty(String line) {
        Pattern pattern = Pattern.compile("(?<key>[\\w\\.]*)\\s*\\:\\s*(?<value>[^;]*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String key = matcher.group("key");
            String value = matcher.group("value");
            return NamedValue.of(key, value);
        } else {
            throw new RuntimeException("Custom property definition does not match format");
        }
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
            length += customPropertyBuffer.position() + 2;
            String line = new String(customPropertyBuffer.array(), UTF).trim();

            return getCustomProperty(line);
        } else {
            stream.reset();
            return null;
        }
    }

    private NamedValue readCustomProperty(SeekableByteChannel channel) throws IOException {
        long position = channel.position();
        ByteBuffer lead = ByteBuffer.allocate(2);
        channel.read(lead);


        if (Arrays.equals(lead.array(), CUSTOM_PROPERTY_HEAD.getBytes())) {
            //reading line
            ByteBuffer customPropertyBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE - 2);
            channel.read(customPropertyBuffer);
            Scanner scanner = new Scanner(channel);
            String line = scanner.nextLine();
            length += line.length() + 2;
            return getCustomProperty(line.trim());
        } else {
            channel.position(position);
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
        props.forEach(this::setValue);
    }

    public void setValue(String name, Object value) {
        setValue(name, Value.of(value));
    }

    public void setValue(String name, Value value) {
        if (Envelope.TYPE_KEY.equals(name)) {
            EnvelopeType type = value.getType() == ValueType.NUMBER ? EnvelopeType.resolve(value.intValue()) : EnvelopeType.resolve(value.stringValue());
            if (type != null) {
                envelopeType = type;
            } else {
                LoggerFactory.getLogger(getClass()).trace("Can't resolve envelope type");
            }
        } else if (Envelope.META_TYPE_KEY.equals(name)) {
            MetaType type = value.getType() == ValueType.NUMBER ? MetaType.resolve((short) value.intValue()) : MetaType.resolve(value.stringValue());
            if (type != null) {
                metaType = type;
            } else {
                LoggerFactory.getLogger(getClass()).error("Can't resolve meta type");
            }
        } else {
            values.put(name, value);
        }
    }

    public int getMetaSize() {
        return values.getOrDefault(Envelope.META_LENGTH_KEY, Value.of(0)).intValue();
    }

    public int getDataSize() {
        return values.getOrDefault(Envelope.DATA_LENGTH_KEY, Value.of(0)).intValue();
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public EnvelopeTag setMetaType(MetaType type) {
        this.metaType = type;
        return this;
    }

    public EnvelopeType getEnvelopeType() {
        return envelopeType;
    }

    public EnvelopeTag setEnvelopeType(EnvelopeType type) {
        this.envelopeType = envelopeType;
        return this;
    }

    public EnvelopeTag read(SeekableByteChannel channel) throws IOException {
        long position = channel.position();
        ByteBuffer lead = ByteBuffer.allocate(2);
        channel.read(lead);

        Map<String, Value> header;

        if (Arrays.equals(lead.array(), LEGACY_START_SEQUENCE)) {
            length = 30;
            ByteBuffer legacyBytes = ByteBuffer.allocate(28);
            channel.read(legacyBytes);
            header = readLegacyHeader(legacyBytes);
        } else if (Arrays.equals(lead.array(), START_SEQUENCE)) {
            length = 20;
            ByteBuffer bytes = ByteBuffer.allocate(18);
            channel.read(bytes);
            header = readHeader(bytes);
        } else if (Arrays.equals(lead.array(), CUSTOM_PROPERTY_HEAD.getBytes())) {
            //No tag
            header = Collections.emptyMap();
            channel.position(position);
            length = 0;
        } else {
            throw new IOException("Wrong start sequence for envelope tag");
        }

        setValues(header);
        //custom properties
        NamedValue value;
        do {
            value = readCustomProperty(channel);
            if (value != null) {
                setValue(value.getName(), value);
            }
        } while (value != null);
        return this;
    }

    public EnvelopeTag read(InputStream stream) throws IOException {

        length = 2;

        Map<String, Value> header;
        stream.mark(30);

        //reading start sequence
        byte[] startSequence = new byte[2];
        stream.read(startSequence);

        if (Arrays.equals(startSequence, LEGACY_START_SEQUENCE)) {
            length += 28;
            byte[] bytes = new byte[28];
            stream.read(bytes);
            header = readLegacyHeader(ByteBuffer.wrap(bytes));
        } else if (Arrays.equals(startSequence, START_SEQUENCE)) {
            length += 18;
            byte[] body = new byte[18];
            stream.read(body);
            header = readHeader(ByteBuffer.wrap(body).order(ByteOrder.BIG_ENDIAN));
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

    public ByteBuffer byteHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put(START_SEQUENCE);

        buffer.putInt(envelopeType.getCode());
        buffer.putShort(metaType.getCodes().get(0));
        buffer.putInt((int) values.get(Envelope.META_LENGTH_KEY).longValue());
        buffer.putInt((int) values.get(Envelope.DATA_LENGTH_KEY).longValue());
        buffer.put(END_SEQUENCE);
        return buffer;
    }


}

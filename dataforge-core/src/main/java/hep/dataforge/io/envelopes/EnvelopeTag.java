package hep.dataforge.io.envelopes;

import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Envelope tag converter v2
 * Created by darksnake on 25-Feb-17.
 */
public class EnvelopeTag {

    private static final byte[] START_SEQUENCE = {'#', '~'};
    private static final byte[] END_SEQUENCE = {'~', '#', '\r', '\n'};

    private Map<String, Value> values = new HashMap<>();
    private MetaType metaType = XMLMetaType.instance;
    private EnvelopeType envelopeType = DefaultEnvelopeType.instance;


    protected byte[] getStartSequence(){
        return new byte[]{'#', '~'};
    }

    protected byte[] getEndSequence(){
        return new byte[]{'~', '#', '\r', '\n'};
    }

    /**
     * Get the length of tag in bytes. -1 means undefined size in case tag was modified
     *
     * @return
     */
    public int getLength() {
        return 20;
    }

    /**
     * Read header line
     *
     * @throws IOException
     */
    protected Map<String, Value> readHeader(ByteBuffer buffer) throws IOException {

        Map<String, Value> res = new HashMap<>();

        byte[] lead = new byte[2];

        buffer.get(lead);

        if (!Arrays.equals(lead, getStartSequence())) {
            throw new IOException("Wrong start sequence for envelope tag");
        }

        //reading type
        int type = buffer.getInt(2);
        EnvelopeType envelopeType = EnvelopeType.resolve(type);

        if (envelopeType != null) {
            res.put(Envelope.TYPE_KEY, Value.of(envelopeType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve envelope type code. Using default");
        }

        //reading meta type
        short metaTypeCode = buffer.getShort(6);
        MetaType metaType = MetaType.resolve(metaTypeCode);

        if (metaType != null) {
            res.put(Envelope.META_TYPE_KEY, Value.of(metaType.getName()));
        } else {
            LoggerFactory.getLogger(EnvelopeTag.class).warn("Could not resolve meta type. Using default");
        }

        //reading meta length
        long metaLength = Integer.toUnsignedLong(buffer.getInt(8));
        res.put(Envelope.META_LENGTH_KEY, Value.of(metaLength));
        //reading data length
        long dataLength = Integer.toUnsignedLong(buffer.getInt(12));
        res.put(Envelope.DATA_LENGTH_KEY, Value.of(dataLength));

        byte[] endSequence = new byte[4];
        buffer.position(16);
        buffer.get(endSequence);

        if (!Arrays.equals(endSequence, getEndSequence())) {
            throw new IOException("Wrong ending sequence for envelope tag");
        }
        return res;
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
        Map<String, Value> header;

        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        channel.read(bytes);
        bytes.flip();
        header = readHeader(bytes);

        setValues(header);
        return this;
    }

    public EnvelopeTag read(InputStream stream) throws IOException {
        Map<String, Value> header;
        byte[] body = new byte[getLength()];
        stream.read(body);
        header = readHeader(ByteBuffer.wrap(body).order(ByteOrder.BIG_ENDIAN));
        setValues(header);
        return this;
    }

    public ByteBuffer toBytes() {
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

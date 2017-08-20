package hep.dataforge.io.envelopes;

import hep.dataforge.io.IOUtils;
import hep.dataforge.meta.Meta;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hep.dataforge.io.envelopes.Envelope.DATA_LENGTH_KEY;

/**
 * A tagless envelope. No tag. Data infinite by default
 */
public class TaglessEnvelopeType implements EnvelopeType {
    public static final String TAGLESS_ENVELOPE_TYPE = "tagless";

    public static final String TAGLESS_ENVELOPE_HEADER = "#~DFTL~#";
    public static final String META_START_PROPERTY = "metaSeparator";
    public static final String DEFAULT_META_START = "#~META~#";
    public static final String DATA_START_PROPERTY = "dataSeparator";
    public static final String DEFAULT_DATA_START = "#~DATA~#";

    public static TaglessEnvelopeType instance = new TaglessEnvelopeType();

    @Override
    public int getCode() {
        return 0x4446544c;//DFTL
    }

    @Override
    public String getName() {
        return TAGLESS_ENVELOPE_TYPE;
    }

    @Override
    public String description() {
        return "Tagless envelope. Text only. By default uses XML meta with utf encoding and data end auto-detection.";
    }

    @Override
    public EnvelopeReader getReader(Map<String, String> properties) {
        return new TaglessReader(properties);
    }

    @Override
    public EnvelopeWriter getWriter(Map<String, String> properties) {
        return new TaglessWriter(properties);
    }

    public static class TaglessWriter implements EnvelopeWriter {
        private Map<String, String> properties;

        public TaglessWriter(Map<String, String> properties) {
            this.properties = properties;
        }

        public TaglessWriter() {
            properties = Collections.emptyMap();
        }

        @Override
        public void write(OutputStream stream, Envelope envelope) throws IOException {
            PrintWriter writer = new PrintWriter(stream);

            //printing header
            writer.println(TAGLESS_ENVELOPE_HEADER);

            //printing all properties
            properties.forEach((key, value) -> writer.printf("#? %s: %s;%n", key, value));
            writer.printf("#? %s: %s;%n", DATA_LENGTH_KEY, envelope.getData().size());

            //Printing meta
            if (envelope.hasMeta()) {
                //print optional meta start tag
                writer.println(properties.getOrDefault(META_START_PROPERTY, DEFAULT_META_START));
                writer.flush();

                //define meta type
                MetaType metaType = MetaType.resolve(properties);

                //writing meta
                metaType.getWriter().write(stream, envelope.meta());
            }

            //Printing data
            if (envelope.hasData()) {
                //print mandatory data start tag
                writer.println(properties.getOrDefault(DATA_START_PROPERTY, DEFAULT_DATA_START));
                writer.flush();
                Channels.newChannel(stream).write(envelope.getData().getBuffer());
            }
            stream.flush();

        }
    }

    public static class TaglessReader implements EnvelopeReader {
        private final Map<String, String> override;

        public TaglessReader(Map<String, String> override) {
            this.override = override;
        }

        @Override
        public Envelope read(InputStream stream) throws IOException {
            Map<String, String> properties = new HashMap<>(override);
            Meta meta = readMeta(stream, properties);
            ByteBuffer data = readData(stream, properties);
            return new EnvelopeBuilder().setMeta(meta).setData(data).build();
        }

        private ByteBuffer readData(InputStream stream, Map<String, String> properties) throws IOException {
            if (properties.containsKey(DATA_LENGTH_KEY)) {
                ByteBuffer buffer = ByteBuffer.allocate(Integer.parseInt(properties.get(DATA_LENGTH_KEY)));
                Channels.newChannel(stream).read(buffer);
                return buffer;
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (stream.available() > 0) {
                    baos.write(stream.read());
                }
                return ByteBuffer.wrap(baos.toByteArray());
            }
        }

        private Meta readMeta(InputStream stream, Map<String, String> properties) throws IOException {
            StringBuilder sb = new StringBuilder();
            String metaEnd = properties.getOrDefault(DATA_START_PROPERTY, DEFAULT_DATA_START);
            String nextLine = IOUtils.forEachLine(
                    stream,
                    "UTF-8",
                    line -> line.trim().equals(metaEnd),
                    line -> {
                        if (line.startsWith("#?")) {
                            readProperty(line, properties);
                        } else if (line.isEmpty() || line.startsWith("#~")) {
                            //Ignore headings, do nothing
                        } else {
                            sb.append(line).append("\r\n");
                        }
                    }
            );

            if (sb.length() == 0) {
                return Meta.empty();
            } else {
                MetaType metaType = MetaType.resolve(properties);
                try {
                    return metaType.getReader().readString(sb.toString());
                } catch (ParseException e) {
                    throw new RuntimeException("Failed to parse meta", e);
                }
            }
        }

        private void readProperty(String line, Map<String, String> properties) {
            Pattern pattern = Pattern.compile("#\\?\\s*(?<key>[\\w\\.]*)\\s*\\:\\s*(?<value>[^;]*);?");
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String key = matcher.group("key");
                String value = matcher.group("value");
                properties.putIfAbsent(key, value);
            } else {
                throw new RuntimeException("Custom property definition does not match format");
            }
        }
    }

}

package hep.dataforge.io.envelopes;

import hep.dataforge.io.IOUtils;
import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by darksnake on 02-Mar-17.
 */
public class BinaryMetaType implements MetaType {
    public static final Short[] BINARY_META_CODES = {0x424d, 10};
    private static BinaryMetaReader reader = new BinaryMetaReader();
    private static BinaryMetaWriter writer = new BinaryMetaWriter();

    @Override
    public List<Short> getCodes() {
        return Arrays.asList(BINARY_META_CODES);
    }

    @Override
    public String getName() {
        return "BIN";
    }

    @Override
    public MetaStreamReader getReader() {
        return reader;
    }

    @Override
    public MetaStreamWriter getWriter() {
        return writer;
    }

    @Override
    public Predicate<String> fileNameFilter() {
        return str -> str.toLowerCase().endsWith(".meta");
    }

    private static class BinaryMetaReader implements MetaStreamReader {

        @Override
        public MetaBuilder read(InputStream stream, long length) throws IOException, ParseException {
            if (length > 0) {
                byte[] bytes = new byte[(int) length];
                stream.read(bytes);
                stream = new ByteArrayInputStream(bytes);
            }
            ObjectInputStream ois = new ObjectInputStream(stream);
            return MetaUtils.readMeta(ois);
        }

        @Override
        public MetaStreamReader withCharset(Charset charset) {
            //charet is ignored
            return this;
        }

        @Override
        public Charset getCharset() {
            return IOUtils.ASCII_CHARSET;
        }
    }

    private static class BinaryMetaWriter implements MetaStreamWriter {

        @Override
        public MetaStreamWriter withCharset(Charset charset) {
            //charset is ignored
            return this;
        }

        @Override
        public void write(OutputStream stream, Meta meta) throws IOException {
            MetaUtils.writeMeta(new ObjectOutputStream(stream), meta);
            stream.write('\r');
            stream.write('\n');
        }
    }
}

package hep.dataforge.io.envelopes;

import hep.dataforge.meta.MetaBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class TaglessEnvelopeTest {
    private Envelope envelope = new EnvelopeBuilder()
            .setMeta(new MetaBuilder()
                    .putValue("myValue", 12)
            ).setData("Всем привет!".getBytes(Charset.forName("UTF-8")));

    private EnvelopeType envelopeType = TaglessEnvelopeType.instance;

    @Test
    public void testWriteRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        envelopeType.getWriter().write(baos, envelope);

        System.out.println(new String(baos.toByteArray()));

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Envelope restored = envelopeType.getReader().read(bais);

        assertEquals(new String(restored.getData().getBuffer().array(),"UTF-8"), "Всем привет!");
    }

    @Test
    public void testShortForm() throws IOException {
        String envString = "<meta myValue=\"12\"/>\n" +
                "#~DATA~#\n" +
                "Всем привет!";
        System.out.println(envString);
        ByteArrayInputStream bais = new ByteArrayInputStream(envString.getBytes("UTF-8"));
        Envelope restored = envelopeType.getReader().read(bais);

        assertEquals(new String(restored.getData().getBuffer().array(),"UTF-8"), "Всем привет!");
    }
}
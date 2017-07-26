package hep.dataforge.io.envelopes;

import hep.dataforge.meta.MetaBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TaglessEnvelopeTest {
    Envelope envelope = new EnvelopeBuilder()
            .setMeta(new MetaBuilder("meta")
                    .putValue("myValue", 12)
            ).setData("Всем привет!".getBytes());

    EnvelopeType envelopeType = TaglessEnvelopeType.instance;

    @Test
    public void testWriteRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        envelopeType.getWriter().write(baos, envelope);

        System.out.println(new String(baos.toByteArray()));

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Envelope restored = envelopeType.getReader().read(bais);

        assertEquals(new String(restored.getData().getBuffer().array()), "Всем привет!");
    }

    @Test
    public void testShortForm() throws IOException {
        String envString = "<meta myValue=\"12\"/>\n" +
                "#~=DATA=~#\n" +
                "Всем привет!";
        System.out.println(new String(envString));
        ByteArrayInputStream bais = new ByteArrayInputStream(envString.getBytes());
        Envelope restored = envelopeType.getReader().read(bais);

        assertEquals(new String(restored.getData().getBuffer().array()), "Всем привет!");
    }
}
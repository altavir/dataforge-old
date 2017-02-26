package hep.dataforge.io.envelopes

import hep.dataforge.data.binary.Binary
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import spock.lang.Specification

/**
 * Created by darksnake on 25-Feb-17.
 */
class EnvelopeFormatSpec extends Specification {
    def "Test read/write"() {
        given:
        byte[] data = "This is my data".bytes
        Meta meta = new MetaBuilder("meta").setValue("myValue", "This is my meta")
        Envelope envelope = new EnvelopeBuilder().setMeta(meta).setData(data).build()
        when:
        def baos = new ByteArrayOutputStream();
        new DefaultEnvelopeWriter().write(baos, envelope)
        byte[] reaArray = baos.toByteArray();
        println new String(reaArray)
        def bais = new ByteArrayInputStream(reaArray)
        Envelope res = new DefaultEnvelopeReader().read(bais)
        then:
        Binary.readToBuffer(res.data).array() == data
    }
}

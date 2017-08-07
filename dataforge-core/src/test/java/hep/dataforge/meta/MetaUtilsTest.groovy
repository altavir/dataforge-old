package hep.dataforge.meta

import hep.dataforge.io.IOUtils
import hep.dataforge.io.XMLMetaReader
import hep.dataforge.io.XMLMetaWriter
import spock.lang.Specification

/**
 * Created by darksnake on 12-Nov-16.
 */
class MetaUtilsTest extends Specification {
    def "serialization test"() {
        given:

        Meta meta = new MetaBuilder("test")
                .setValue("childValue", 18.5)
                .setValue("numeric", 6.2e-8)
                .setNode(new MetaBuilder("childNode").setValue("listValue", [2, 4, 6]).setValue("grandChildValue", true))

        println "initial meta: \n${meta.toString()}"
        when:

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MetaUtils.writeMeta(new ObjectOutputStream(baos), meta);
        byte[] bytes = baos.toByteArray();

        println "Serialized string: \n${new String(bytes, IOUtils.ASCII_CHARSET)}\n"

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        Meta reconstructed = MetaUtils.readMeta(new ObjectInputStream(bais))
        println "reconstructed meta: \n${reconstructed.toString()}"
        then:
        reconstructed == meta
    }

    def "XML reconstruction test"() {
        given:

        Meta meta = new MetaBuilder("test")
                .setValue("childValue", 18.5)
                .setValue("numeric", 6.2e-8)
                .setNode(new MetaBuilder("childNode").setValue("listValue", [2, 4, 6]).setValue("grandChildValue", true))

        println "initial meta: \n${meta.toString()}"
        when:

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new XMLMetaWriter().write(baos, meta);
        byte[] bytes = baos.toByteArray();

        println "XML : \n${new String(bytes, IOUtils.UTF8_CHARSET)}\n"

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        Meta reconstructed = new XMLMetaReader().read(bais)
        println "reconstructed meta: \n${reconstructed.toString()}"
        then:
        reconstructed == meta

    }

    def "test query"() {
        when:
        Meta meta = new MetaBuilder("test")
                .putNode(new MetaBuilder("child").putValue("value", 2))
                .putNode(new MetaBuilder("child").putValue("value", 3).putValue("check",true))
                .putNode(new MetaBuilder("child").putValue("value", 4))
                .putNode(new MetaBuilder("child").putValue("value", 5))
        then:
        meta.getMeta("child[value = 3, check = true]").getValue("check")
    }
}

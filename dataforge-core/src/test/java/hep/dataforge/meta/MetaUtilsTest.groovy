package hep.dataforge.meta

import spock.lang.Specification

/**
 * Created by darksnake on 12-Nov-16.
 */
class MetaUtilsTest extends Specification {
    def "serialization test"() {
        given:

        Meta meta = new MetaBuilder("test")
                .setValue("childValue", 18.5)
                .setNode(new MetaBuilder("childNode").setValue("listValue", [2, 4, 6]).setValue("grandChildValue", true))
        println "initial meta: \n${meta.toString()}"
        when:

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MetaUtils.writeMeta(new ObjectOutputStream(baos),meta);
        byte[] bytes = baos.toByteArray();

        println "Serialized string: \n${new String(bytes)}\n"

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        Meta reconstructed = MetaUtils.readMeta(new ObjectInputStream(bais))
        println "reconstructed meta: \n${reconstructed.toString()}"
        then:
        reconstructed == meta

    }
}

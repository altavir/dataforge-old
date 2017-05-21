package hep.dataforge.io

import hep.dataforge.meta.MetaBuilder
import spock.lang.Specification

class XMLIOTest extends Specification {


    def "XML IO"() {
        given:
        def testMeta =
                new MetaBuilder("test")
                        .putValue("numeric", 22.5)
                        .putValue("other", "otherValue")
                        .putValue("some.path", true)
                        .putNode(
                        new MetaBuilder("child")
                                .putValue("childValue", "childValue")
                                .putNode(
                                new MetaBuilder("grandChild")
                                        .putValue("grandChildValue", "grandChildValue")
                        ))
                        .putNode(
                        new MetaBuilder("child")
                                .putValue("childValue", "otherChildValue")
                                .putNode(
                                new MetaBuilder("grandChild")
                                        .putValue("grandChildValue", "otherGrandChildValue")
                        )
                ).build();
        when:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new XMLMetaWriter().write(baos, testMeta)
        def bytes = baos.toByteArray();
        def res = new XMLMetaReader().read(new ByteArrayInputStream(bytes))
        then:
        res == testMeta
    }

    def "XMlinput"(){
        given:
        def source = "<loader index=\"timestamp\" name=\"msp802596725\" type=\"point\">\n" +
                "    <format>\n" +
                "        <column name=\"timestamp\" type=\"TIME\"/>\n" +
                "        <column name=\"2\" type=\"NUMBER\"/>\n" +
                "        <column name=\"3\" type=\"NUMBER\"/>\n" +
                "        <column name=\"4\" type=\"NUMBER\"/>\n" +
                "        <column name=\"5\" type=\"NUMBER\"/>\n" +
                "        <column name=\"6\" type=\"NUMBER\"/>\n" +
                "        <column name=\"12\" type=\"NUMBER\"/>\n" +
                "        <column name=\"14\" type=\"NUMBER\"/>\n" +
                "        <column name=\"18\" type=\"NUMBER\"/>\n" +
                "        <column name=\"22\" type=\"NUMBER\"/>\n" +
                "        <column name=\"28\" type=\"NUMBER\"/>\n" +
                "        <column name=\"32\" type=\"NUMBER\"/>\n" +
                "    </format>\n" +
                "</loader>"
        when:
        def res = new XMLMetaReader().read(new ByteArrayInputStream(source.bytes))
        then:
        res.getInt("format.column[2].name") == 3
    }
}

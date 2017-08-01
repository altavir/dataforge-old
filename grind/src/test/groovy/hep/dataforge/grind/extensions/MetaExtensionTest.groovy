package hep.dataforge.grind.extensions

import hep.dataforge.grind.Grind
import hep.dataforge.meta.Meta
import spock.lang.Specification

class MetaExtensionTest extends Specification {

    def "Property access"(){
        given:
        Meta meta = Grind.buildMeta(a:22,b:"asdfg")
        when:
        meta.child = Grind.buildMeta(b:33)
        then:
        meta["child.b"] == 33
        meta.child["b"] == 33
    }

}

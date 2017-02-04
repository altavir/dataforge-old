package hep.dataforge.grind

import hep.dataforge.meta.Meta
import spock.lang.Specification

/**
 * Created by darksnake on 04-Aug-16.
 */
class WorkspaceSpecTest extends Specification {
    def "Test meta builder delegation"() {
        given:
            def closure = {
                myMeta(myPar: "val", myOtherPar: 28) {
                    childNode(childValue: true)
                    otherChildNode {
                        grandChildNode(grandChildValue: 88.6)
                    }
                }
            }
        when:
            def metaSpec = new GrindMetaBuilder()
            def metaExec = closure.rehydrate(metaSpec, this, this);
            metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
            def res = metaExec()
        then:
//            println res.getString("otherChildNode.grandChildNode.grandChildValue")
            res.getBoolean("childNode.childValue");
    }

    def "Test meta from string"() {
        given:
        String metaStr = """
                myMeta(myPar: "val", myOtherPar: 28) {
                    childNode(childValue: true)
                    otherChildNode {
                        grandChildNode(grandChildValue: 88.6)
                    }
                }
        """
        when:
            Meta meta = Grind.parseMeta(metaStr);
        then:
            meta.getName() == "myMeta"
            meta.getDouble("otherChildNode.grandChildNode.grandChildValue") == 88.6
    }
}

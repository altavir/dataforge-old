package hep.dataforge.grind

import spock.lang.Specification

/**
 * Created by darksnake on 04-Aug-16.
 */
class WorkspaceBuilderTest extends Specification {
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
            println res.getString("otherChildNode.grandChildNode.grandChildValue")
            res.getBoolean("childNode.childValue");
    }
}

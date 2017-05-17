package hep.dataforge.grind

import hep.dataforge.data.DataNode
import spock.lang.Specification

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindWorkspaceBuilderTest extends Specification {


    def "Run Task"() {
        given:
        GrindWorkspaceBuilder launcher = new GrindWorkspaceBuilder().read {
            getClass().getResourceAsStream('/workspace/workspace.groovy').newReader()
        }
        when:
        DataNode res = launcher.run("testTask")
        res.dataStream().forEach{ println("${it.name}: ${it.get()}")}
        then:
        res.compute("a") == 4;
    }

    def "Run Task with meta"() {
        given:
        GrindWorkspaceBuilder launcher = new GrindWorkspaceBuilder().read {
            getClass().getResourceAsStream('/workspace/workspace.groovy').newReader()
        }
        when:
        DataNode res = launcher.run("testTask{childNode(metaValue: 18); otherChildNode(val: false)}")
        res.dataStream().forEach{ println("${it.name}: ${it.get()}")}
        then:
        res.compute("meta.childNode.metaValue") == 18
    }
}

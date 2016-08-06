package hep.dataforge.grind

import hep.dataforge.data.DataNode
import spock.lang.Specification

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindLauncherTest extends Specification {
    def "Run Task"() {
        given:
        GrindLauncher launcher = new GrindLauncher().from { this.getClass().getResource('/workspace/workspace.groovy').toURI() }
        when:
        DataNode res = launcher.run("testTask")
        res.forEachWithType(Object){key,value -> println("${key}: ${value.get()}")}
        then:
        res.compute("a") == 4;
    }

    def "Run Task with meta"() {
        given:
        GrindLauncher launcher = new GrindLauncher().from { this.getClass().getResource('/workspace/workspace.groovy').toURI() }
        when:
        DataNode res = launcher.run("testTask{childNode(metaValue: 18); otherChildNode(val: false)}")
        res.forEachWithType(Object){key,value -> println("${key}: ${value.get()}")}
        then:
        res.compute("meta.childNode.metaValue") as Double == 18
    }
}

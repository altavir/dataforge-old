package hep.dataforge.grind

import hep.dataforge.data.DataNode
import spock.lang.Specification

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindLauncherTest extends Specification {
    def "RunTask"() {
        given:
        GrindLauncher launcher = new GrindLauncher().from { this.getClass().getResource('/workspace/workspace.groovy').toURI() }
        when:
        DataNode res = launcher.runTask("testTask")
        res.forEachWithType(Object){key,value -> println("${key}: ${value.get()}")}
        then:
        4 == res.compute("a").doubleValue()
    }
}

package hep.dataforge.grind.workspace

import spock.lang.Specification
import spock.lang.Timeout

class ExecTest extends Specification {

    @Timeout(3)
    def "test singleton exec"() {
        given:
        def exec = new ExecSpec()
        exec.with{
            output {
                redirect()
            }
            cli {
                append "java"
                append "-version"
            }
        }
        def action = exec.build()
//        def data = DataUtils.singletonNode("test","test")
        when:
        def res = action.simpleRun("test")
        then:
        println "Result:"
        println res
    }
}

package hep.dataforge.grind.workspace

import spock.lang.Specification
import spock.lang.Timeout

class ExecTest extends Specification {

    @Timeout(3)
    def "get Java version"() {
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

    @Timeout(3)
    def "run python script"(){
        given:
        def exec = new ExecSpec()
        exec.with{
            output {
                process.consumeProcessOutputStream()
                redirect()
            }
            cli {
                append "python"
                argument  context.getClassLoader().getResource('workspace/test.py')
                append "-d 1"
                append "-r OK"
            }
        }
        when:
        def res = exec.build().simpleRun("test");
        println "Result: $res"
        then:
        res.endsWith "OK"
    }
}

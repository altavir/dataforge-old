package hep.dataforge.grind.workspace

import spock.lang.Specification
import spock.lang.Timeout

class ExecTest extends Specification {

    @Timeout(3)
    def "get Java version"() {
        given:
        def exec = new ExecSpec()
        exec.with{
            cli {
                append "java"
                append "-version"
            }
            output {
                println "Out: " + out
                println "Err: " + err
                return err.split()[0]
            }
        }
        def action = exec.build()
        when:
        def res = action.simpleRun("test")
        then:
        res == "java"
    }

    @Timeout(5)
    def "run python script"(){
        given:
        def exec = new ExecSpec()
        exec.with{
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
        res.trim().endsWith "OK"
    }
}

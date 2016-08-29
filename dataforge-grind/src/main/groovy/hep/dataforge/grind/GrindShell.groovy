package hep.dataforge.grind

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
class GrindShell {
    //TODO encapsulate launcher
    GrindLauncher launcher = new GrindLauncher()
    private GroovyShell shell;
    //ConsoleReader console = new ConsoleReader(System.in,System.out);

    GrindShell() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars("java.lang.Math");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);
        Binding binding = new Binding();
        binding.setProperty("df", launcher);
        shell = new GroovyShell(getClass().classLoader, binding, configuration);
    }


    String eval(String expression) {
        return shell.evaluate(expression);
    }

    def start() {
//        while(true) {
//            String expression = console.readLine(">");
//            if ("exit" == expression) {
//                break;
//            }
//            try {
//                console.println(eval(expression));
//            } catch (Exception ex) {
//                ex.printStackTrace(new PrintWriter(console.getOutput()));
//            }
//        }

        BufferedReader reader = System.in.newReader()
        while (true) {
            print(">")
            String expression = reader.readLine();
            if ("exit" == expression) {
                break;
            }
            try {
                println(eval(expression));
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}

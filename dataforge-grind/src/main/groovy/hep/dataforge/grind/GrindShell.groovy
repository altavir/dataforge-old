package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.GlobalContext
import hep.dataforge.grind.plots.PlotHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * A REPL Groovy shell with embedded DataForge features
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
class GrindShell {
    //TODO encapsulate launcher
    @Lazy
    GrindLauncher launcher = new GrindLauncher()
    @Lazy
    PlotHelper plot = new PlotHelper(context);
    private GroovyShell shell;
    private Context context = GlobalContext.instance();
    //ConsoleReader console = new ConsoleReader(System.in,System.out);

    GrindShell(Context context) {
        this();
        this.context = context
    }

    GrindShell() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars("java.lang.Math");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);
        Binding binding = new Binding();
        binding.setProperty("df", launcher);
        binding.setProperty("plt", plot);
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

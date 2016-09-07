package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.GlobalContext
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.grind.plots.PlotHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.util.function.Consumer

/**
 * A REPL Groovy shell with embedded DataForge features
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
class GrindShell {
    private Binding binding = new Binding();
    private GroovyShell shell;
    Set<Hook> hooks = new HashSet<>();

    GrindShell() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars("java.lang.Math");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);
        binding.setProperty("buildWorkspace", { String fileName -> new GrindLauncher().from(new File(fileName)).buildWorkspace() })
        binding.setProperty("context", GlobalContext.instance())
        binding.setProperty("plt", new PlotHelper(GlobalContext.instance()))
        shell = new GroovyShell(getClass().classLoader, binding, configuration);
    }

    def setContext(Context context) {
        println("df: Using context ${context.getName()}")
        bind("context", context);

        println("df: Resetting plot environment")
        PlotHelper plot = new PlotHelper(context);
        bind("plt", plot);
    }

    def bind(String key, Object value) {
        binding.setProperty(key, value)
    }


    String eval(String expression) {
        Object res = shell.evaluate(expression);
        return postEval(res);
    }

    /**
     * Post evaluate result. Compute lazy data and use smart data visualization
     * @param res
     * @return
     */
    def postEval(Object res) {
        if (res instanceof DataNode) {
            def node = res.computeAll();
            node.dataStream().map { it.get() }.forEach { postEval(it) };
            return;
        } else if (res instanceof Data) {
            res = res.get();
        }
        hooks.each {
            it.accept(res);
        }
        return res;
    }

    def println(String str) {
        System.out.println(str)
    }

    def print(String str) {
        System.out.print(str)
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
                if (expression != null) {
                    println(eval(expression));
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * Start using provided closure as initializing script
     * @param closure
     */
    def start(Closure closure) {
        this.with(closure)
        start()
    }

    /**
     * A consumer that applies only to given type
     * @param < T >
     */
    class Hook<T> {
        private final Class<T> type;
        private final Consumer<T> consumer;

        Hook(Class<T> type, Consumer<T> consumer) {
            this.type = type
            this.consumer = consumer
        }

        void accept(Object t) {
            if (type.isInstance(t)) {
                consumer.accept(t as T);
            }
        }
    }
}

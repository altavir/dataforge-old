package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.grind.plots.PlotHelper
import hep.dataforge.io.BasicIOManager
import hep.dataforge.io.IOUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.DumbTerminal
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

import java.util.function.Consumer

/**
 * A REPL Groovy shell with embedded DataForge features
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
class GrindShell {
    private static final AttributedStyle RES = AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW);
    private static final AttributedStyle PROMPT = AttributedStyle.BOLD.foreground(AttributedStyle.CYAN);
    private static final AttributedStyle DEFAULT = AttributedStyle.DEFAULT;

    private Binding binding = new Binding();
    private GroovyShell shell;
    private Context context = Global.instance();
    private Set<Hook> hooks = new HashSet<>();
    private Terminal terminal;

    GrindShell() {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars("java.lang.Math");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);

        //define help closure
        def help = this.&help;
        binding.setProperty("man", help);
        binding.setProperty("help", help);

        //define important properties
        binding.setProperty("context", context)
        binding.setProperty("plots", new PlotHelper(Global.instance()))
        shell = new GroovyShell(getClass().classLoader, binding, configuration);

    }

    def help(){
        println("In order to display state of object and show help type `help <object>`");
    }

    def help(Object obj) {
        if(obj == null){
            help()
        } else {
            try {
                println(obj.invokeMethod("help", null))
            } catch (Exception ex) {
                println("No manual or help article for ${obj.class}")
            }
        }
    }

    /**
     * Build default jline console based on operating system. Do not use for preview inside IDE
     * @return
     */
    GrindShell withSystemTerminal() {
        this.terminal = TerminalBuilder.builder()
                .name("df")
                .system(true)
                .jna(true)
                .encoding("UTF-8")
                .build()
        return this
    }

    GrindShell withTerminal(Terminal terminal) {
        this.terminal = terminal
        return this
    }

    private Terminal getTerminal(){
        if (terminal == null) {
            terminal = new DumbTerminal(System.in, System.out);
            terminal.echo(false);
        }
        return terminal;
    }

    def setContext(Context context) {
        println("df: Using context ${context.getName()}")
        bind("context", context);
        this.context = context;

        println("df: Resetting plot environment")
        PlotHelper plot = new PlotHelper(context);
        bind("plt", plot);
    }

    def GrindShell withContext(Context context) {
        setContext(context)
        return this;
    }

    def bind(String key, Object value) {
        binding.setProperty(key, value)
    }


    protected synchronized String eval(String expression) {
        Object res = shell.evaluate(expression);
        //remembering last answer
        bind("res", res);
        //TODO remember n last answers
        return postEval(res);
    }

    /**
     * Post evaluate result. Compute lazy data and use smart data visualization
     * @param res
     * @return
     */
    protected def postEval(Object res) {
        if(res instanceof Closure){
            res = res.call()
        }
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
        getTerminal().writer().with {
            println(str);
            flush();
        }
    }

    def print(String str) {
        getTerminal().writer().with {
            print(str)
        }
    }

    def launch() {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(getTerminal())
                .appName("DataForge Grind terminal")
                .build();
        PrintWriter writer = getTerminal().writer();

        context.setIO(new BasicIOManager(getTerminal().output(), getTerminal().input()));
//        def appender = TerminalLogLayout.buildAppender(context.logger.loggerContext, terminal);
//        context.logger.addAppender(appender)

        def promptLine = new AttributedString("[${context.getName()}] --> ", PROMPT).toAnsi(getTerminal());
        try {
            while (true) {
                String expression = reader.readLine(promptLine);
                if ("exit" == expression|| expression == null) {
                    break;
                }
                try {
                    def res = eval(expression);
                    if (res != null) {
                        if(getTerminal() instanceof DumbTerminal){
                            println(" = ${res.toString()}")
                        } else {
                            def resStr = new AttributedStringBuilder()
                                    .style(RES)
                                    .append("\tres = ")
                                    .style(DEFAULT)
                                    .append(res.toString());
                            println(resStr.toAnsi(getTerminal()))
                        }
                    }
                } catch (Exception ex) {
                    writer.print(IOUtils.ANSI_RED);
                    ex.printStackTrace(writer);
                    writer.print(IOUtils.ANSI_RESET);
                }
            }
        } catch (UserInterruptException ex) {
            writer.println("Interrupted by user")
        }catch (EndOfFileException){
            writer.println("Terminated by user")
        } finally {
            getTerminal().close()
        }

    }

    /**
     * Start using provided closure as initializing script
     * @param closure
     */
    def launch(Closure closure) {
        this.with(closure)
        launch()
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

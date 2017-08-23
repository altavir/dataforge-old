package hep.dataforge.grind.terminal

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.description.*
import hep.dataforge.grind.Grind
import hep.dataforge.grind.GrindShell
import hep.dataforge.io.IOUtils
import hep.dataforge.io.markup.Markedup
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.io.markup.MarkupUtils
import hep.dataforge.meta.Meta
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.names.Named
import hep.dataforge.plots.jfreechart.JFCFrameFactory
import hep.dataforge.values.ValueType
import hep.dataforge.workspace.FileBasedWorkspace
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.DumbTerminal
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

import java.time.Duration
import java.util.stream.Stream

/**
 * A REPL Groovy shell with embedded DataForge features
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
@ValuesDefs([
        @ValueDef(name = "evalClosures", type = ValueType.BOOLEAN, def = "true", info = "Automatically replace closures by their results"),
        @ValueDef(name = "evalData", type = ValueType.BOOLEAN, def = "false", info = "Automatically replace data by its value"),
        @ValueDef(name = "unwrap", type = ValueType.BOOLEAN, def = "false", info = "Apply result hooks for each element of collection or stream")
])
class GrindTerminal extends SimpleConfigurable {
    private static final AttributedStyle RES = AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW);
    private static final AttributedStyle PROMPT = AttributedStyle.BOLD.foreground(AttributedStyle.CYAN);
    private static final AttributedStyle DEFAULT = AttributedStyle.DEFAULT;

    private final GrindShell shell;
    private final Terminal terminal;

    /**
     * Build default jline console based on operating system. Do not use for preview inside IDE
     * @return
     */
    static GrindTerminal system(Context context = Global.instance()) {
        context.logger.debug("Starting grind terminal using system shell")
        return new GrindTerminal(context,
                TerminalBuilder.builder()
                               .name("df")
                               .system(true)
                               .encoding("UTF-8")
                               .build()
        )
    }

    static GrindTerminal dumb(Context context = Global.instance()) {
        context.logger.debug("Starting grind terminal using dumb shell")
        return new GrindTerminal(context);
    }

    /**
     * Apply some closure to each of sub-results using shell configuration
     * @param res
     * @return
     */
    def unwrap(Object res, Closure cl = { it }) {
        if (getConfig().getBoolean("evalClosures", true) && res instanceof Closure) {
            res = (res as Closure).call()
        } else if (getConfig().getBoolean("evalData", true) && res instanceof Data) {
            res = (res as Data).get();
        } else if (res instanceof DataNode) {
            (res as DataNode).dataStream().forEach { unwrap(it, cl) };
        }

        if (getConfig().getBoolean("unwrap", true)) {
            if (res instanceof Collection) {
                (res as Collection).forEach { unwrap(it, cl) }
            } else if (res instanceof Stream) {
                (res as Stream).forEach { unwrap(it, cl) }
            }
        }
        cl.call(res);
    }

    GrindTerminal(Context context, Terminal terminal = null) {

        //start fx plugin in global
        Global.instance().pluginManager().load("hep.dataforge:fx");

        //define terminal if it is not defined
        if (terminal == null) {
            terminal = new DumbTerminal(System.in, System.out);
            terminal.echo(false);

//            def console = System.console()
//            if (console) {
//                console.readLine('> Please enter your username: ')
//            }

        }


        this.terminal = terminal
        context.logger.debug("Using ${terminal.class} terminal")

        //build shell context
        if (Global.instance() == context) {
            context = Global.getContext("GRIND");
            context.pluginManager().load("hep.dataforge:plots-fx")
            JFCFrameFactory.setDefault(context);
            //FIXME There is some bug in the groovy compilation here
//            InputStream inputStream = System.in;
//            OutputStream outputStream = System.out
//            context.pluginManager().load(new BasicIOManager(inputStream, outputStream));
        }

        //create the shell
        shell = new GrindShell(context)

        Meta markupConfig = Grind.buildMeta(target: "terminal")
        TerminalMarkupRenderer renderer = new TerminalMarkupRenderer(terminal);

        //bind helper commands

        shell.bind("show") { res ->
            if (res instanceof Markedup) {
                renderer.ln()
                if (res instanceof Named) {
                    renderer.render(MarkupBuilder.text((res as Named).name, "red").build())
                    renderer.ln()
                }
                renderer.render((res as Markedup).markup(markupConfig))
            }
            return null;
        }

        shell.bind("describe") { it ->
            if (it instanceof Described) {
                renderer.render(MarkupUtils.markupDescriptor(it as Described))
            } else if (it instanceof NodeDescriptor) {
                renderer.render(MarkupUtils.markupDescriptor(it))
            } else if (it instanceof String) {
                NodeDescriptor descriptor = DescriptorUtils.buildDescriptor(it);
                if (descriptor.meta().isEmpty()) {
                    renderer.render(MarkupBuilder.text("The description for ")
                                                 .addText("${it}", "blue")
                                                 .addText(" is empty")
                                                 .build()
                    )
                } else {
                    renderer.render(MarkupUtils.markupDescriptor(descriptor))
                }
            } else {
                MarkupBuilder builder = MarkupBuilder.text("No description found for ").addText("${it}", "blue")
                renderer.render(builder.build());
            }
            renderer.ln()
            return null;
        }

        //binding.setProperty("man", help);
        shell.bind("help", this.&help);

        //binding workspace build from default location
        File wsFile = new File("workspace.groovy");
        if (wsFile.exists()) {
            try {
                context.logger.info("Found 'workspace.groovy' in default location. Using it to build workspace.")
                shell.bind("ws", FileBasedWorkspace.build(context, wsFile.toPath()));
                context.logger.info("Workspace builder bound to 'ws'")
            } catch (Exception ex) {
                context.logger.error("Failed to build workspace from 'workspace.groovy'", ex)
            }
        }
    }

    def help() {
        println("In order to display state of object and show help type `help <object>`");
    }

    def help(Object obj) {
        if (obj == null) {
            help()
        } else {
            TerminalMarkupRenderer renderer = new TerminalMarkupRenderer(terminal);
            try {
                def res = obj.invokeMethod("help", null);
                if (res instanceof Markup) {

                    renderer.render(res)
                    renderer.ln()
                } else {
                    println(res);
                }
                println();
            } catch (Exception ignored) {
                renderer.render(MarkupBuilder.text("No help article for ").addText("${obj}", "blue").build());
            }
        }
    }

    Terminal getTerminal() {
        return terminal;
    }

    GrindShell getShell() {
        return shell
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

    private def eval(String expression) {
        def start = System.currentTimeMillis()
        def res = unwrap(shell.eval(expression))
        def now = System.currentTimeMillis()
        if (meta().getBoolean("benchmark", true)) {
            Duration duration = Duration.ofMillis(now - start);
            shell.context.logger.info("Expression $expression evaluated in $duration")
        }
        return res;
    }

    /**
     * Start the terminal
     * @return
     */
    def launch() {
        LineReader reader = LineReaderBuilder.builder()
                                             .terminal(getTerminal())
                                             .appName("DataForge Grind terminal")
                                             .build();
        PrintWriter writer = getTerminal().writer();

//
//        def appender = TerminalLogLayout.buildAppender(context.logger.loggerContext, terminal);
//        context.logger.addAppender(appender)

        def promptLine = new AttributedString("[${shell.context.getName()}] --> ", PROMPT).toAnsi(getTerminal());
        try {
            while (true) {
                String expression = reader.readLine(promptLine);
                if ("exit" == expression || expression == null) {
                    shell.getContext().logger.debug("Exit command received")
                    break;
                }
                try {
                    def res = eval(expression);
                    if (res != null) {
                        String str = res.toString();

//                        //abbreviating the result
//                        //TODO improve string abbreviation
//                        if (str.size() > 50) {
//                            str = str[0..50] + "..."
//                        }

                        def resStr = new AttributedStringBuilder()
                                .style(RES)
                                .append("\tres = ")
                                .style(DEFAULT)
                                .append(str);
                        println(resStr.toAnsi(getTerminal()))
                    }
                } catch (Exception ex) {
                    writer.print(IOUtils.ANSI_RED);
                    ex.printStackTrace(writer);
                    writer.print(IOUtils.ANSI_RESET);
                }
            }
        } catch (UserInterruptException ignored) {
            writer.println("Interrupted by user")
        } catch (EndOfFileException ignored) {
            writer.println("Terminated by user")
        } finally {
            shell.getContext().logger.debug("Closing terminal")
            getTerminal().close()
            shell.getContext().logger.debug("Terminal closed")
        }

    }

    /**
     * Start using provided closure as initializing script
     * @param closure
     */
    def launch(@DelegatesTo(GrindShell) Closure closure) {
        this.shell.with(closure)
        launch()
    }


}

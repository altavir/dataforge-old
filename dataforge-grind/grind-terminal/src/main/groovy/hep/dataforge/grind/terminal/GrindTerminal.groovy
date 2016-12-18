package hep.dataforge.grind.terminal

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.grind.GrindShell
import hep.dataforge.io.BasicIOManager
import hep.dataforge.io.IOUtils
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.DumbTerminal
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

/**
 * A REPL Groovy shell with embedded DataForge features
 * Created by darksnake on 29-Aug-16.
 */
@CompileStatic
class GrindTerminal {
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
        return new GrindTerminal(context,
                TerminalBuilder.builder()
                        .name("df")
                        .system(true)
                        .jna(true)
                        .encoding("UTF-8")
                        .build()
        )
    }

    static GrindTerminal dumb(Context context = Global.instance()) {
        return new GrindTerminal(context, null);
    }

    GrindTerminal(Context context, Terminal terminal) {
        if (terminal == null) {
            terminal = new DumbTerminal(System.in, System.out);
            terminal.echo(false);
        }
        this.terminal = terminal
        if (Global.instance() == context) {
            context = Global.getContext("GRIND");
            context.pluginManager().loadPlugin("hep.dataforge:plots-jfc")
            context.setIO(new BasicIOManager(terminal.output(), terminal.input()));
        }
        shell = new GrindShell(context)

        //define help closure
        def help = this.&help;
        //binding.setProperty("man", help);
        shell.bind("help", help);
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
                println("No help article for ${obj}")
            }
        }
    }

    private Terminal getTerminal() {
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
                    break;
                }
                try {
                    def res = shell.eval(expression);
                    if (res != null) {
                        def resStr = new AttributedStringBuilder()
                                .style(RES)
                                .append("\tres = ")
                                .style(DEFAULT)
                                .append(res.toString());
                        println(resStr.toAnsi(getTerminal()))
                    }
                } catch (Exception ex) {
                    writer.print(IOUtils.ANSI_RED);
                    ex.printStackTrace(writer);
                    writer.print(IOUtils.ANSI_RESET);
                }
            }
        } catch (UserInterruptException ex) {
            writer.println("Interrupted by user")
        } catch (EndOfFileException) {
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

}

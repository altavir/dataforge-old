package hep.dataforge.grind.terminal

import hep.dataforge.io.text.Markup
import hep.dataforge.io.text.MarkupBuilder
import hep.dataforge.io.text.MarkupUtils
import hep.dataforge.plots.PlotDataAction
import hep.dataforge.tables.ListTable
import hep.dataforge.tables.Table
import org.jline.terminal.impl.DumbTerminal
import spock.lang.Specification

/**
 * Created by darksnake on 04-Jan-17.
 */
class TerminalMarkupRendererTest extends Specification {
    TerminalMarkupRenderer r;

    void setup() {
        def terminal = new DumbTerminal(System.in, System.out)
        r = new TerminalMarkupRenderer(terminal);
        r.forceANSI = true
    }

    def "Test terminal markup"() {
        when:
        MarkupBuilder builder = new MarkupBuilder()
                .addText("this is my text ")
                .addList(
                MarkupBuilder.text("first line"),
                MarkupBuilder.text("second line"),
                MarkupBuilder.text("red line", "red"),
                new MarkupBuilder().addText("sub list").addList(
                        MarkupBuilder.text("first line"),
                        MarkupBuilder.text("second line"),
                        MarkupBuilder.text("colored line", "cyan")
                ),
                MarkupBuilder.text("blue line", "blue")
        )
                .addText("text end")
        Markup markup = builder.build();
        then:
        println(markup.meta.toString())
        r.render(markup);
        println()
    }

    def "Test descriptor formatting"() {
        when:
        Markup markup = MarkupUtils.markupDescriptor(new PlotDataAction().getDescriptor())
        then:
        println(markup.meta.toString())
        r.render(markup)
        println()
    }

    def "Test table display"() {
        when:
        Table table = new ListTable.Builder("x", "y", "z")
                .row(1, 2, 3)
                .row(4.5, 5.678, -2)
                .row(0, 0, 0)
                .build()
        Markup markup = MarkupUtils.markupTable(table);
        then:
        println(markup.meta.toString())
        r.render(markup)
        println()
    }
}

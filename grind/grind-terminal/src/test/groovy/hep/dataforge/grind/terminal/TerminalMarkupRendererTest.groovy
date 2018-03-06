package hep.dataforge.grind.terminal

import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupGroup
import hep.dataforge.io.markup.MarkupUtils
import hep.dataforge.meta.Meta
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
        MarkupGroup markup = new MarkupGroup().with {
            it.text("this is my text ", ""){}
            it.list {
                it.text("first line", "") {}
                it.text("second line", "") {}
                it.text("red line", "red") {}
                it.text("sub list", "") {}
                it.list {
                    it.text("first line", "") {}
                    it.text("second line", "") {}
                    it.text("colored line", "cyan") {}
                }
                it.text("blue line", "blue") {}
            }
            it.text("text end", "") {}
            it
        }

        then:
        println(markup.toMeta().toString())
        r.render(markup);
        println()
    }

    def "Test descriptor formatting"() {
        when:
        Markup markup = MarkupUtils.markupDescriptor(new PlotDataAction().getDescriptor())
        then:
        println(markup.toMeta())
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
        Markup markup = table.markup(Meta.empty())
        then:
        //println(markup.toMeta())
        r.render(markup)
        println()
    }

}

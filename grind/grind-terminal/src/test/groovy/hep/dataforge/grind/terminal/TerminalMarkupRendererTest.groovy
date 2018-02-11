package hep.dataforge.grind.terminal

import hep.dataforge.grind.GrindMarkupBuilder
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder
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
        MarkupBuilder builder = new MarkupBuilder()
                .text("this is my text ")
                .list(
                MarkupBuilder.text("first line"),
                MarkupBuilder.text("second line"),
                MarkupBuilder.text("red line", "red"),
                new MarkupBuilder().text("sub list").list(
                        MarkupBuilder.text("first line"),
                        MarkupBuilder.text("second line"),
                        MarkupBuilder.text("colored line", "cyan")
                ),
                MarkupBuilder.text("blue line", "blue")
        )
                .text("text end")
        Markup markup = builder.build();
        then:
        println(markup.toMeta().toString())
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
        Markup markup = table.markup(Meta.empty())
        then:
        //println(markup.toMeta())
        r.render(markup)
        println()
    }


    def "Test markup builder"() {
        when:
        Markup markup = new GrindMarkupBuilder().markup {
            meta(someKey: "someValue")
            text color: "red", "this is my red text "
            text color: "blue", "and blue text"
            list(bullet: "\$ ") {
                text "first line"
                text "second line"
                group {
                    style(bold: true, italic: true)
                    text "sub list:"
                    list(bullet: "* ") {
                        text "sub list line"
                        text "another one"
                    }
                }
                group("style.color": "red") {
                    text "composite "
                    text color: "blue", "text"
                }
            }
            text bold: true, "\n***table test***\n"
            table {
                style (textWidth: 10){
                    styleChild(val:true)
                }
                row {
                    text "a"
                    text color: "cyan", "b"
                    text "c"
                }
                row(["d", "e", "f"])
            }
        }
        then:
        println markup.meta
        r.render(markup)
    }
}

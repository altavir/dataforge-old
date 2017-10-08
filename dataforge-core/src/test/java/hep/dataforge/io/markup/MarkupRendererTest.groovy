package hep.dataforge.io.markup

import hep.dataforge.tables.ListTable
import hep.dataforge.tables.Table
import spock.lang.Specification

/**
 * Created by darksnake on 03-Jan-17.
 */
class MarkupRendererTest extends Specification {

    def "Test simple renderer"() {
        given:
        SimpleMarkupRenderer r = new SimpleMarkupRenderer(System.out);
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
                        MarkupBuilder.text("colored line", "red")
                ),
                MarkupBuilder.text("blue line", "blue")
        )
                .text("text end")
        Markup markup = builder.build();
        then:
        r.render(markup);
    }

    def "Test html renderer"() {
        given:
        HTMLMarkupRenderer r = new HTMLMarkupRenderer(System.out);
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
                        MarkupBuilder.text("colored line", "red")
                ),
                MarkupBuilder.text("blue line", "blue")
        )
                .text("text end")

        Table table = new ListTable.Builder("x", "y", "z")
                .row(1, 2, 3)
                .row(4.5, 5.678, -2)
                .row(0, 0, 0)
                .build()

        def tableMarkup = MarkupUtils.markupTable(table).setValue("html.width","100%");
        builder.content(tableMarkup);
        Markup markup = builder.build();
        then:
        println(markup.meta.toString())
        r.render(markup);
    }
}

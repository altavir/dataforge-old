package hep.dataforge.io.text

import spock.lang.Specification

/**
 * Created by darksnake on 03-Jan-17.
 */
class StreamMarkupRendererTest extends Specification {

    def "Test simple markup"() {
        given:
        SimpleMarkupRenderer r = new SimpleMarkupRenderer(System.out);
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
                            MarkupBuilder.text("colored line", "red")
                    ),
                    MarkupBuilder.text("blue line", "blue")
                )
                .addText("text end")
        Markup markup = builder.build();
        then:
        r.render(markup);
    }
}

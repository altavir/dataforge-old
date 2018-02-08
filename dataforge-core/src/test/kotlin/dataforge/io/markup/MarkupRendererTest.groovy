/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dataforge.io.markup

import hep.dataforge.io.markup.*
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

/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.tables;

import hep.dataforge.io.markup.Markedup;
import hep.dataforge.io.markup.Markup;
import hep.dataforge.io.markup.MarkupBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaMorph;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static hep.dataforge.io.markup.GenericMarkupRenderer.TABLE_TYPE;

/**
 * An immutable table of values
 *
 * @author Alexander Nozik
 */
public interface Table extends PointSource, MetaMorph, Markedup, RowProvider {

    Column getColumn(String name);

    /**
     * Apply row-based transformation
     *
     * @param streamTransform
     * @return
     */
    Table transform(UnaryOperator<Stream<DataPoint>> streamTransform);

    @Override
    default Markup markup(Meta configuration) {
        MarkupBuilder builder = new MarkupBuilder().setType(TABLE_TYPE);
        //render header
        builder.addContent(new MarkupBuilder()
                .setValue("header", true)
                .setType("tr") //optional
                .setContent(getFormat().getColumns().map(col -> MarkupBuilder.text(col.getTitle())))
        );
        //render table itself
        forEach(dp -> {
            builder.addContent(new MarkupBuilder()
                    .setType("td") // optional
                    .setContent(getFormat().getColumns().map(col -> MarkupBuilder.text(dp.getString(col.getName())))));
        });
        return builder.build();
    }
}

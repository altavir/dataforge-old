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
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaMorph;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static hep.dataforge.io.markup.GenericMarkupRenderer.TABLE_TYPE;

/**
 * An immutable table of values
 *
 * @author Alexander Nozik
 */
public interface Table extends Markedup, NavigableValuesSource, MetaMorph {

    /**
     * Get an immutable column from this table
     *
     * @param name
     * @return
     */
    Column getColumn(String name);

    /**
     * Get columns as a stream
     * @return
     */
    Stream<Column> getColumns();

    /**
     * Get a specific value
     * @param columnName the name of the column
     * @param rowNumber the number of the row
     * @return
     */
    Value get(String columnName, int rowNumber);

    @NotNull
    @Override
    default Markup markup(@NotNull Meta configuration) {
        MarkupBuilder builder = new MarkupBuilder().setType(TABLE_TYPE);
        //render header
        builder.content(new MarkupBuilder()
                .setValue("header", true)
                .setType("tr") //optional
                .setContent(getFormat().getColumns().map(col -> new MarkupBuilder().text(col.getTitle())))
        );
        //render table itself
        forEach(dp -> {
            builder.content(new MarkupBuilder()
                    .setType("td") // optional
                    .setContent(getFormat().getColumns().map(col -> new MarkupBuilder().text(dp.getString(col.getName())))));
        });
        return builder.build();
    }

    /**
     * A minimal set of fields to be displayed in this table. Could return empty format if source is unformatted
     *
     * @return
     */
    TableFormat getFormat();

    @NotNull
    @Override
    default Meta toMeta() {
        MetaBuilder res = new MetaBuilder("table");
        res.putNode("format", getFormat().toMeta());
        MetaBuilder dataNode = new MetaBuilder("data");
        forEach(dp -> dataNode.putNode("point", dp.toMeta()));
        res.putNode(dataNode);
        return res;
    }

//    default Meta toMeta() {
//        MetaBuilder res = new MetaBuilder("table");
//        res.putNode("format", getFormat().toMeta());
//        MetaBuilder dataNode = new MetaBuilder("data");
//        forEach(dp -> dataNode.putNode("point", dp.toMeta()));
//        res.putNode(dataNode);
//        return res;
//    }
}

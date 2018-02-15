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

import hep.dataforge.io.markup.*;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaMorph;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;


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
     *
     * @return
     */
    Stream<Column> getColumns();

    /**
     * Get a specific value
     *
     * @param columnName the name of the column
     * @param rowNumber  the number of the row
     * @return
     */
    Value get(String columnName, int rowNumber);

    @NotNull
    @Override
    default Markup markup(@NotNull Meta configuration) {
        TableMarkup builder = new TableMarkup();

        //render header
        RowMarkup header = new RowMarkup(builder);
        getFormat().getColumns().map(col -> TextMarkup.Companion.create(col.getTitle())).forEach(header::add);
        builder.setHeader(header);


        //render table itself
        forEach(dp -> {
            RowMarkup row = new RowMarkup(builder);
            getFormat().getColumns().map(col -> TextMarkup.Companion.create(dp.getString(col.getName()))).forEach(row::add);
            builder.addRow(row);
        });
        return builder;
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

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

import hep.dataforge.description.NodeDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.meta.SimpleMetaMorph;
import hep.dataforge.names.Names;
import hep.dataforge.values.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;

/**
 * A class for point set visualization
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "column", multiple = true, required = true, info = "A column format", from = "class::hep.dataforge.tables.ColumnFormat")
@NodeDef(name = "defaultColumn", info = "Default column format. Used when format for specific column is not given")
@NodeDef(name = DEFAULT_META_NAME, info = "Custom table information")
public class MetaTableFormat extends SimpleMetaMorph implements TableFormat {

    /**
     * An empty format holding information only about the names of columns
     *
     * @param names
     * @return
     */
    public static TableFormat forNames(String[] names) {
        MetaBuilder builder = new MetaBuilder("format");
        for (String n : names) {
            builder.putNode(new MetaBuilder("column").setValue("name", n));
        }
        return new MetaTableFormat(builder.build());
    }


    public static TableFormat forNames(Iterable<String> names) {
        return forNames(StreamSupport.stream(names.spliterator(), false).toArray(String[]::new));
    }

    /**
     * Build a table format using given data point as reference
     *
     * @param dataPoint
     * @return
     */
    public static TableFormat forValues(Values dataPoint) {
        MetaBuilder builder = new MetaBuilder("format");
        for (String n : dataPoint.getNames()) {
            builder.putNode(new MetaBuilder("column").setValue("name", n).setValue("type", dataPoint.getValue(n).getType().name()));
        }
        return new MetaTableFormat(builder.build());
    }


    public MetaTableFormat(Meta meta) {
        //TODO add transformation to use short column description
        super(meta);
    }

    @Override
    public Names getNames() {
        return Names.of(getColumns().map(ColumnFormat::getName).collect(Collectors.toList()));
    }

    public Meta getColumnMeta(String column) {
        return MetaUtils.findNodeByValue(getMeta(), "column", "name", column).orElseThrow(() -> new NameNotFoundException(column));
    }

    @Override
    public ColumnFormat getColumn(String column) {
        return new ColumnFormat(getColumnMeta(column));
    }

    @Override
    public Stream<ColumnFormat> getColumns() {
        return getMeta().getMetaList("column").stream().map(ColumnFormat::new);
    }

    public boolean isEmpty() {
        return !getMeta().hasMeta("column");
    }


    @NotNull
    @Override
    public Iterator<ColumnFormat> iterator() {
        return getColumns().iterator();
    }

}

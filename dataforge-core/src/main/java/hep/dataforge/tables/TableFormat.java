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
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.values.ValueFormatFactory;
import hep.dataforge.values.ValueFormatter;
import hep.dataforge.values.ValueType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class for point set visualization
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "column", multiple = true, info = "A column format")
@NodeDef(name = "defaultColumn", info = "Default column format")
public class TableFormat implements Annotated, NameSetContainer, Serializable {

    private Meta meta;
    private Names names;
    private final Map<String, ValueFormatter> formats = new HashMap<>();

    public static TableFormat fromMeta(Meta meta) {
        if (meta.hasMeta("column")) {
            return new TableFormat(meta);
        } else if (meta.hasValue("names")) {
            return TableFormat.forNames(meta.getStringArray("names"));
        } else {
            return empty();
        }
    }

    public static Meta toMeta(TableFormat format) {
        return format.meta;
    }

    public static TableFormat empty() {
        return forNames();
    }

    public static TableFormat fixedWidth(int width, String... names) {
        return TableFormat.fixedWidth(width, Arrays.asList(names));
    }

    public static TableFormat forNames(String... names) {
        return forNames(Arrays.asList(names));
    }

    public static TableFormat forNames(Iterable<String> names) {
        MetaBuilder meta = new MetaBuilder("format");
        for (String name : names) {
            meta.putNode(new MetaBuilder("column").setValue("name", name));
        }
        return new TableFormat(meta);
    }

    public static TableFormat fixedWidth(int width, Iterable<String> names) {
        MetaBuilder meta = new MetaBuilder("format");
        for (String name : names) {
            meta.putNode(new MetaBuilder("column")
                    .setValue("name", name)
                    .setValue("width", width));
        }
        return new TableFormat(meta);
    }

    public static TableFormat forPoint(DataPoint point) {
        MetaBuilder meta = new MetaBuilder("format");
        for (String name : point.namesAsArray()) {
            meta.putNode(new MetaBuilder("column")
                    .setValue("name", name)
                    .setValue("type", point.getValue(name).valueType().name()));
        }
        return new TableFormat(meta);
    }

    public TableFormat(Meta meta) {
        this.meta = meta;
    }

    /**
     * A set of names for this tableFormat. Empty names corresponds to
     * unformatted table
     *
     * @return
     */
    @Override
    public Names names() {
        if (this.names == null) {
            names = Names.of(meta().getMetaList("column").stream().map(node -> node.getString("name", "")).collect(Collectors.toList()));
        }
        return this.names;
    }

    @ValueDef(name = "name", required = true, info = "Column name")
    @ValueDef(name = "type", multiple = true,
            info = "Allowed type for this column. Multiple types are allowed. First type is considered to be primary")
    @ValueDef(name = "title", info = "A column title. By default equals column name")
    @ValueDef(name = "role", info = "A role of this column")
    private Optional<? extends Meta> findColumnMeta(String columnName) {
        return meta().getMetaList("column").stream().filter(column -> columnName.equals(column.getString("name"))).findFirst();
    }

    public Meta getColumnMeta(String columnName) {
        Optional<Meta> op = findColumnMeta(columnName).map(m -> m);
        return op.orElse(getDefaultColumnMeta());
    }

    protected Meta getDefaultColumnMeta() {
        if (meta.hasMeta("defaultColumn")) {
            return meta.getMeta("defaultColumn");
        } else {
            return new MetaBuilder("column").build();
        }
    }

    public ValueFormatter getValueFormat(String name) {
        return formats.computeIfAbsent(name, (String columnName) -> {
            Meta columnMeta = getColumnMeta(columnName);
            if (columnMeta != null) {
                return ValueFormatFactory.build(columnMeta);
            } else {
                return ValueFormatFactory.EMPTY_FORMAT;
            }
        });
    }

    /**
     * Format data point as a string
     *
     * @param point
     * @return
     */
    public String format(DataPoint point) {
        return names()
                .asList()
                .stream()
                .map((name) -> getValueFormat(name).format(point.getValue(name)))
                .collect(Collectors.joining("\t"));
    }

    public String getTitle(String columnName) {
        return getColumnMeta(columnName).getString("title", columnName);
    }

    public String getRole(String columnName) {
        return getColumnMeta(columnName).getString("role", "");
    }

    public ValueType getType(String columnName) {
        return ValueType.valueOf(getColumnMeta(columnName).getString("type", ValueType.STRING.name()));
    }

    /**
     * A subset of current format
     *
     * @param newNames
     * @return
     */
    public TableFormat subSet(String... newNames) {
        //Если список пустой, значит допустимы все имена
        if (this.names.asList().isEmpty()) {
            return this;
        }
        MetaBuilder newMeta = new MetaBuilder("format");
        for (String newName : newNames) {
            newMeta.putNode(findColumnMeta(newName).orElseThrow(() -> new NameNotFoundException(newName)));
        }
        return new TableFormat(newMeta.build());
    }

    @Override
    public Meta meta() {
        return meta;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(this.meta());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        this.meta = (Meta) in.readObject();
    }

}

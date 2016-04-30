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

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.ValueType;
import java.util.LinkedHashMap;
import java.util.Map;

public class TableFormatBuilder {

    Map<String, MetaBuilder> columns;
    MetaBuilder defaultColumn;

    public TableFormatBuilder() {
        columns = new LinkedHashMap<>();
    }

    public TableFormatBuilder(String... names) {
        this();
        for (String name : names) {
            addName(name);
        }
    }

    public TableFormatBuilder(Iterable<String> names) {
        this();
        for (String name : names) {
            addName(name);
        }
    }

    private MetaBuilder addName(String name) {
        if (!columns.containsKey(name)) {
            MetaBuilder columnBuilder = new MetaBuilder(name);
            columns.put(name, columnBuilder);
            return columnBuilder;
        } else {
            throw new NamingException("Dublicate name");
        }
    }

    public TableFormatBuilder addColumn(String name) {
        addName(name);
        return this;
    }

    public TableFormatBuilder addColumn(String name, String title, ValueType type) {
        addName(name).setValue("title", title).setValue("type", type.toString());
        return this;
    }

    public TableFormatBuilder addColumn(String name, String title, int width, ValueType type) {
        addName(name).setValue("title", title).setValue("type", type.toString()).setValue("width", width);
        return this;
    }

    public TableFormatBuilder addColumn(String name, ValueType type) {
        addName(name).setValue("type", type.toString());
        return this;
    }

    public TableFormatBuilder addColumn(String name, int width, ValueType type) {
        addName(name).setValue("type", type.toString()).setValue("width", width);
        return this;
    }

    public TableFormatBuilder addString(String name) {
        return addColumn(name, ValueType.STRING);
    }

    public TableFormatBuilder addNumber(String name) {
        return addColumn(name, ValueType.NUMBER);
    }

    public TableFormatBuilder addTime(String name) {
        return addColumn(name, ValueType.TIME);
    }

    public TableFormatBuilder setType(String name, ValueType... type) {
        if (!columns.containsKey(name)) {
            addName(name);
        }
        for (ValueType t : type) {
            columns.get(name).putValue("type", t.toString());
        }
        return this;
    }

    public TableFormatBuilder setRole(String name, String... role) {
        if (!columns.containsKey(name)) {
            addName(name);
        }
        for (String r : role) {
            columns.get(name).putValue("role", r);
        }
        return this;
    }

    public TableFormatBuilder setTitle(String name, String title) {
        if (!columns.containsKey(name)) {
            addName(name);
        }
        columns.get(name).putValue("title", title);
        return this;
    }

    public TableFormatBuilder setWidth(String name, int width) {
        if (!columns.containsKey(name)) {
            addName(name);
        }
        columns.get(name).putValue("width", width);
        return this;
    }

    public TableFormat build() {
        MetaBuilder builder = new MetaBuilder("format");
        for (Meta m : columns.values()) {
            builder.putNode(m);
        }
        if (defaultColumn != null) {
            builder.setNode("defaultColumn", defaultColumn);
        }
        return new TableFormat(builder.build());
    }

}

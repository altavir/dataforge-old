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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple immutable Column implementation using list of values
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public final class ListColumn implements Column, MetaMorph {

    /**
     * Create a copy of given column if it is not ListColumn.
     *
     * @param column
     * @return
     */
    public static ListColumn copy(Column column) {
        if (column instanceof ListColumn) {
            return (ListColumn) column;
        } else {
            return new ListColumn(column.getFormat(), column.stream());
        }
    }

    /**
     * Create a copy of given column renaming it in process
     *
     * @param name
     * @param column
     * @return
     */
    public static ListColumn copy(String name, Column column) {
        if (name.equals(column.getName())) {
            return copy(column);
        } else {
            return new ListColumn(ColumnFormat.rename(name, column.getFormat()), column.stream());
        }
    }

    public static ListColumn build(ColumnFormat format, Stream<?> values) {
        return new ListColumn(format, values.map(ValueFactory::of));
    }

    private ColumnFormat format;
    private List<Value> values;

    public ListColumn() {
    }

    ListColumn(Meta meta) {
        this.format = MetaMorph.Companion.morph(ColumnFormat.class, meta.getMeta("format"));
        this.values = meta.getValue("data").getList();
    }

    public ListColumn(ColumnFormat format, Stream<Value> values) {
        this.format = format;
        this.values = values.collect(Collectors.toList());
        if (!this.values.stream().allMatch(format::isAllowed)) {
            throw new IllegalArgumentException("Not allowed value in the column");
        }
    }

    @Override
    public List<Value> asList() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public ColumnFormat getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value get(int n) {
        return values.get(n);
    }

    public Stream<Value> stream() {
        return values.stream();
    }

    @NotNull
    @Override
    public Iterator<Value> iterator() {
        return values.iterator();
    }

    @Override
    public int size() {
        return values.size();
    }

    @NotNull
    @Override
    public Meta toMeta() {
        return new MetaBuilder("column")
                .putNode("format", format.toMeta())
                .putValue("data", values);
    }
}

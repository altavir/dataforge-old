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

import hep.dataforge.exceptions.DataFormatException;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An immutable row-based Table based on ArrayList. Row access is fast, but
 * column access could be complicated
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ListTable extends ListOfPoints implements Table, MetaMorph {

    public static ListTable copy(@NotNull Table table) {
        if (table instanceof ListTable) {
            return (ListTable) table;
        } else {
            return new ListTable(table.getFormat(),table.getRows());
        }
    }

    /**
     * Формат описывает набор полей, которые ОБЯЗАТЕЛЬНО присутствуют в каждой
     * точке из набора данных. Набор полей каждой точки может быть шире, но не
     * уже.
     */
    private TableFormat format;

    private ListTable(TableFormat format) {
        this.format = format;
    }

    /**
     * constructor for deserialization
     */
    public ListTable() {
    }

    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param format a {@link MetaTableFormat} object.
     * @param points a {@link java.lang.Iterable} object.
     */
    public ListTable(TableFormat format, Iterable<Values> points) {
        this.format = format;
        if (points != null) {
            addRows(points);
        }
    }

    public ListTable(TableFormat format, Stream<Values> points) {
        this.format = format;
        if (points != null) {
            addRows(points.collect(Collectors.toList()));
        }
    }

    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param points a {@link java.util.List} object.
     */
    public ListTable(List<Values> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Can't create ListTable from the empty list. Format required.");
        }
        this.format = MetaTableFormat.forPoint(points.get(0));
        addRows(points);
    }


    protected void addRow(Values e) throws NamingException {
        if (format.getNames().size() == 0 || e.getNames().contains(format.getNames())) {
            this.data.add(e);
        } else {
            throw new DataFormatException("The input data point doesn't contain all required fields.");
        }
    }
//
//    @Override
//    public Table transform(UnaryOperator<Stream<Values>> streamTransform) {
//        return new ListTable(getFormat(), streamTransform.apply(stream()));
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableFormat getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     *
     * @param columnName
     * @return
     */
    @Override
    public Column getColumn(String columnName) throws NameNotFoundException {
        if (!this.format.getNames().contains(columnName)) {
            throw new NameNotFoundException(columnName);
        }
        return new Column() {
            @Override
            public ColumnFormat getFormat() {
                return ListTable.this.getFormat().getColumn(columnName);
            }

            @Override
            public Value get(int n) {
                return asList().get(n);
            }

            @Override
            public List<Value> asList() {
                return StreamSupport.stream(this.spliterator(), false).collect(Collectors.toList());
            }

            public Stream<Value> stream() {
                return ListTable.this.getRows().map(point -> point.getValue(columnName));
            }

            @NotNull
            @Override
            public Iterator<Value> iterator() {
                return stream().iterator();
            }

            @Override
            public int size() {
                return ListTable.this.size();
            }
        };
    }

    @Override
    public Stream<Column> getColumns() {
        return getFormat().getNames().stream().map(this::getColumn);
    }

    @Override
    public Value get(String columnName, int rowNumber) {
        return getRow(rowNumber).getValue(columnName);
    }

    @Override
    public Meta toMeta() {
        MetaBuilder res = new MetaBuilder("table");
        res.putNode("format", getFormat().toMeta());
        MetaBuilder dataNode = new MetaBuilder("data");
        forEach(dp -> dataNode.putNode("point", dp.toMeta()));
        res.putNode(dataNode);
        return res;
    }

    @Override
    public void fromMeta(Meta meta) {
        if (this.format != null || !data.isEmpty()) {
            throw new NonEmptyMetaMorphException(getClass());
        }
        format = new MetaTableFormat(meta.getMeta("format"));
        data.addAll(ListOfPoints.buildFromMeta(meta.getMeta("data")));
    }

    public static class Builder {

        private ListTable table;

        public Builder(TableFormat format) {
            table = new ListTable(format);
        }

        public Builder(Iterable<String> format) {
            table = new ListTable(MetaTableFormat.forNames(format));
        }

        public Builder(String... format) {
            table = new ListTable(MetaTableFormat.forNames(format));
        }

        public Builder() {
            table = new ListTable(new MetaTableFormat(Meta.empty()));
        }

//        public Builder format(Consumer<TableFormatBuilder> consumer){
//            TableFormatBuilder formatBuilder = new TableFormatBuilder();
//            consumer.accept(formatBuilder);
//            table.format = formatBuilder.builder();
//            return this;
//        }

        /**
         * Если formatter == null, то могут быть любые точки
         *
         * @param e
         * @throws hep.dataforge.exceptions.NamingException if any.
         */
        public Builder row(Values e) throws NamingException {
            table.addRow(e);
            return this;
        }

        /**
         * Add new point constructed from a list of objects using current table format
         *
         * @param values
         * @return
         * @throws NamingException
         */
        public Builder row(Object... values) throws NamingException {
            table.addRow(ValueMap.of(table.format.namesAsArray(), values));
            return this;
        }

        public Builder row(ValueProvider values) {
            String[] names = table.format.namesAsArray();
            Map<String, Value> map = Stream.of(names).collect(Collectors.toMap(name -> name, values::getValue));
            table.addRow(new ValueMap(map));
            return this;
        }

        public Builder rows(Iterable<? extends Values> points) {
            table.addRows(points);
            return this;
        }

        public Builder rows(Stream<? extends Values> stream) {
            stream.forEach(it -> table.addRow(it));
            return this;
        }
        //TODO make methods to add virtual columns

        public Table build() {
            return table;
        }
    }

}

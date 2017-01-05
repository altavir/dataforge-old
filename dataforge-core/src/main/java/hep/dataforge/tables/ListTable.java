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
import hep.dataforge.values.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The Table implementation using list of DataPoints. Row access is fast, but
 * column access could be complicated
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ListTable implements Table {

    private final ArrayList<DataPoint> data = new ArrayList<>();

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
     * Проверяет, что все точки соответствуют формату
     *
     * @param format a {@link hep.dataforge.tables.TableFormat} object.
     * @param points a {@link java.lang.Iterable} object.
     */
    public ListTable(TableFormat format, Iterable<DataPoint> points) {
        this.format = format;
        if (points != null) {
            addRows(points);
        }
    }

    public ListTable(TableFormat format, Stream<DataPoint> points) {
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
    public ListTable(List<DataPoint> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Can't create ListTable from the empty list. Format required.");
        }
        this.format = TableFormat.forPoint(points.get(0));
        addRows(points);
    }

    /**
     * Если formatter == null, то могут быть любые точки
     *
     * @param e a {@link hep.dataforge.tables.DataPoint} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    private void addRow(DataPoint e) throws NamingException {
        if (format.names().size() == 0 || e.names().contains(format.names())) {
            this.data.add(e);
        } else {
            throw new DataFormatException("The input data point doesn't contain all required fields.");
        }
    }

    private void addRows(Iterable<? extends DataPoint> points) {
        for (DataPoint dp : points) {
            addRow(dp);
        }
    }

    @Override
    public Table transform(UnaryOperator<Stream<DataPoint>> streamTransform) {
        return new ListTable(getFormat(), streamTransform.apply(stream()));
    }

    /**
     * {@inheritDoc}
     *
     * @param i
     * @return
     */
    @Override
    public DataPoint getRow(int i) {
        return data.get(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableFormat getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value getValue(int index, String name) throws NameNotFoundException {
        return this.data.get(index).getValue(name);
    }

    /**
     * {@inheritDoc}
     *
     * @param columnName
     * @return
     */
    @Override
    public Column getColumn(String columnName) throws NameNotFoundException {
        if (!this.format.names().contains(columnName)) {
            throw new NameNotFoundException(columnName);
        }
        return new Column() {
            @Override
            public ColumnFormat getFormat() {
                return ListTable.this.getFormat().getColumnFormat(columnName);
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
                return ListTable.this.stream().map(point -> point.getValue(columnName));
            }

            @Override
            public Iterator<Value> iterator() {
                return stream().iterator();
            }

            @Override
            public Meta meta() {
                return ListTable.this.getFormat().getColumnMeta(columnName);
            }

            @Override
            public int size() {
                return ListTable.this.size();
            }
        };
    }

    /**
     * Get a copy of given column. Data is not synchronized
     *
     * @param columnName
     * @return
     */
    public Column getColumnCopy(String columnName) {
        return new ListColumn(getFormat().getColumnMeta(columnName), getColumn(columnName).asList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataPoint> iterator() {
        return data.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return data.size();
    }

    /**
     * Clear all data in the Table. Does not affect annotation.
     */
    public void clear() {
        this.data.clear();
    }

    @Override
    public Meta toMeta() {
        MetaBuilder res = new MetaBuilder("table");
        res.putNode("format", format.toMeta());
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
        format = new TableFormat(meta.getNode("format"));
        data.addAll(DataPoint.buildFromMeta(meta.getNode("data")));
    }

    public static class Builder {

        private ListTable table;

        public Builder(TableFormat format) {
            table = new ListTable(format);
        }

        public Builder(Iterable<String> format) {
            table = new ListTable(TableFormat.forNames(format));
        }

        public Builder(String... format) {
            table = new ListTable(TableFormat.forNames(format));
        }

        public Builder() {
            table = new ListTable(new TableFormat(Meta.empty()));
        }

        /**
         * Если formatter == null, то могут быть любые точки
         *
         * @param e a {@link hep.dataforge.tables.DataPoint} object.
         * @throws hep.dataforge.exceptions.NamingException if any.
         */
        public Builder row(DataPoint e) throws NamingException {
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
            table.addRow(new MapPoint(table.format.namesAsArray(), values));
            return this;
        }

        public Builder rows(Iterable<? extends DataPoint> points) {
            table.addRows(points);
            return this;
        }
        //TODO make methods to add virtual columns

        public Table build() {
            return table;
        }
    }

}

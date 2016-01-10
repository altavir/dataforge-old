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
package hep.dataforge.data;

import hep.dataforge.exceptions.DataFormatException;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.meta.Meta;
import hep.dataforge.content.NamedMetaHolder;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Список DataPoint, который следит за тем, чтобы добавляемые точки
 * соответстаовали заданному формату
 *
 * Работа с колонками может быть затруднена
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ListDataSet extends NamedMetaHolder implements DataSet {

    private final ArrayList<DataPoint> data = new ArrayList<>();

    /**
     * Формат описывает набор полей, которые ОБЯЗАТЕЛЬНО присутствуют в каждой
     * точке из набора данных. Набор полей каждой точки может быть шире, но не
     * уже.
     */
    private final DataFormat format;

    public ListDataSet(String name, DataFormat format) {
        super(name);
        this.format = format;
    }

    public ListDataSet(DataFormat format) {
        super();
        this.format = format;
    }

    public ListDataSet(String... format) {
        super();
        this.format =  DataFormat.forNames(format);
    }

    public ListDataSet(String name, String[] format) {
        super(name);
        this.format =  DataFormat.forNames(format);
    }

    public ListDataSet(String name) {
        super(name);
        this.format = new DataFormat();
    }

    /**
     * По-умолчанию делаем набор данных анонимным, чтобы не было накладок с
     * конструктором
     */
    public ListDataSet() {
        this.format = new DataFormat();
    }
    
    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param format a {@link hep.dataforge.data.DataFormat} object.
     * @param points a {@link java.lang.Iterable} object.
     */
    public ListDataSet(String name, Meta annotation, Iterable<DataPoint> points, DataFormat format) {
        super(name, annotation);
        this.format = format;
        if (points != null) {
            addAll(points);
        }
    }

    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param points a {@link java.util.List} object.
     */
    public ListDataSet(String name, Meta annotation, List<DataPoint> points) {
        super(name, annotation);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Can't create ListDataSet from the empty list. Format required.");
        }
        this.format = DataFormat.forPoint(points.get(0));
        addAll(points);
    }

    /**
     * Если format == null, то могут быть любые точки
     *
     * @param e a {@link hep.dataforge.data.DataPoint} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    public void add(DataPoint e) throws NamingException {
        if (format.getDimension() == 0 || e.names().contains(format)) {
            this.data.add(e);
        } else {
            throw new DataFormatException("The input data point doesn't contain all required fields.");
        }
    }

    public final void addAll(Iterable<? extends DataPoint> points) {
        for (DataPoint dp : points) {
            add(dp);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     */
    @Override
    public ListDataSet filter(Predicate<DataPoint> condition) throws NamingException {
        ListDataSet res = new ListDataSet(getName(), this.getDataFormat());
        for (DataPoint dp : this) {
            if (condition.test(dp)) {
                res.add(dp);
            }
        }
        return res;
    }

    /**
     * {@inheritDoc}
     *
     * @param i
     * @return
     */
    @Override
    public DataPoint get(int i) {
        return data.get(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataFormat getDataFormat() {
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
     * @param name
     * @return
     */
    @Override
    public Column getColumn(String name) throws NameNotFoundException {
        if (!this.format.contains(name)) {
            throw new NameNotFoundException(name);
        }
        ArrayList<Value> values = new ArrayList<>();
        for (DataPoint point : this) {
            values.add(point.getValue(name));
        }
        return new ListColumn(values);
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
     * {@inheritDoc}
     *
     * sorts dataset and returns sorted one
     */
    @Override
    public ListDataSet sort(String name, boolean ascending) {
        ListDataSet res = new ListDataSet(getName(), this.getDataFormat());
        res.addAll(this);
        res.data.sort((DataPoint o1, DataPoint o2) -> {
            if (ascending) {
                return o1.getValue(name).compareTo(o2.getValue(name));
            } else {
                return -o1.getValue(name).compareTo(o2.getValue(name));
            }
        });
        res.setMeta(meta());
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet sort(Comparator<DataPoint> comparator) {
        ListDataSet res = new ListDataSet(getName(), this.getDataFormat());
        res.addAll(this);
        res.data.sort(comparator);
        res.setMeta(meta());
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<DataPoint> stream() {
        return this.data.stream();
    }

    /**
     * Clear all data in the DataSet. Does not affect annotation.
     */
    public void clear() {
        this.data.clear();
    }
}

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
package hep.dataforge.points;

import hep.dataforge.exceptions.DataFormatException;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
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
public class ListPointSet implements PointSet {

    private final ArrayList<DataPoint> data = new ArrayList<>();

    /**
     * Формат описывает набор полей, которые ОБЯЗАТЕЛЬНО присутствуют в каждой
     * точке из набора данных. Набор полей каждой точки может быть шире, но не
     * уже.
     */
    private final PointFormat format;

    public ListPointSet(PointFormat format) {
        this.format = format;
    }

    public ListPointSet(String... format) {
        this.format = PointFormat.forNames(format);
    }

    public ListPointSet() {
        this.format = new PointFormat();
    }

    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param format a {@link hep.dataforge.points.PointFormat} object.
     * @param points a {@link java.lang.Iterable} object.
     */
    public ListPointSet(PointFormat format, Iterable<DataPoint> points) {
        this.format = format;
        if (points != null) {
            addAll(points);
        }
    }

    public ListPointSet(PointFormat format, Stream<DataPoint> points) {
        this.format = format;
        if (points != null) {
            addAll(points.collect(Collectors.toList()));
        }
    }

    @Override
    public PointSet subSet(UnaryOperator<Stream<DataPoint>> streamTransform) {
        return new ListPointSet(getFormat(), streamTransform.apply(stream()));
    }

    /**
     * Проверяет, что все точки соответствуют формату
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param points a {@link java.util.List} object.
     */
    public ListPointSet(List<DataPoint> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Can't create ListPointSet from the empty list. Format required.");
        }
        this.format = PointFormat.forPoint(points.get(0));
        addAll(points);
    }

    /**
     * Если format == null, то могут быть любые точки
     *
     * @param e a {@link hep.dataforge.points.DataPoint} object.
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
    public PointFormat getFormat() {
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
     * Clear all data in the PointSet. Does not affect annotation.
     */
    public void clear() {
        this.data.clear();
    }

}

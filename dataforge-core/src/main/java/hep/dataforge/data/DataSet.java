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

import hep.dataforge.content.Content;
import static hep.dataforge.data.DataFiltering.getTagCondition;
import static hep.dataforge.data.DataFiltering.getValueCondition;
import hep.dataforge.values.Value;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>
 * DataSet interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface DataSet extends Iterable<DataPoint>, Content {

    /**
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     *
     * @param condition a {@link java.util.function.Predicate} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     * @return a {@link hep.dataforge.data.DataSet} object.
     */
    DataSet filter(Predicate<DataPoint> condition) throws NamingException;

    /**
     * Быстрый фильтр для значений одного поля
     *
     * @param valueName
     * @param a
     * @param b
     * @return
     * @throws hep.dataforge.exceptions.NamingException
     */
    default DataSet filter(String valueName, Value a, Value b) throws NamingException {
        return this.filter(getValueCondition(valueName, a, b));
    }

    default DataSet filter(String valueName, double a, double b) throws NamingException {
        return this.filter(getValueCondition(valueName, Value.of(a), Value.of(b)));
    }

    /**
     * Быстрый фильтр по меткам
     *
     * @param tags
     * @throws hep.dataforge.exceptions.NamingException
     * @return a {@link hep.dataforge.data.Column} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    default DataSet filter(String... tags) throws NamingException {
        return this.filter(getTagCondition(tags));
    }

    DataPoint get(int i);
    Column getColumn(String name) throws NameNotFoundException;

    /**
     * <p>
     * getDataFormat.</p>
     *
     * @return a {@link hep.dataforge.data.DataFormat} object.
     */
    DataFormat getDataFormat();

    /**
     * <p>
     * getValue.</p>
     *
     * @param index a int.
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.Value} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    Value getValue(int index, String name) throws NameNotFoundException;

    /**
     * <p>
     * size.</p>
     *
     * @return a int.
     */
    int size();

    /**
     * sorts dataset and returns sorted one
     *
     * @param name a {@link java.lang.String} object.
     * @param ascending a boolean.
     * @return a {@link hep.dataforge.data.DataSet} object.
     */
    DataSet sort(String name, boolean ascending);

    /**
     * <p>
     * sort.</p>
     *
     * @param comparator a {@link java.util.Comparator} object.
     * @return a {@link hep.dataforge.data.DataSet} object.
     */
    DataSet sort(Comparator<DataPoint> comparator);
    
    /**
     * <p>stream.</p>
     *
     * @return a {@link java.util.stream.Stream} object.
     */
    Stream<DataPoint> stream();

}
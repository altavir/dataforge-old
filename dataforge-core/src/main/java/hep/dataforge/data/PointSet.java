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
import static hep.dataforge.data.Filtering.getTagCondition;
import static hep.dataforge.data.Filtering.getValueCondition;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>
 PointSet interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface PointSet extends Iterable<DataPoint>, Content {

    /**
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     *
     * @param condition a {@link java.util.function.Predicate} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     * @return a {@link hep.dataforge.data.PointSet} object.
     */
    PointSet filter(Predicate<DataPoint> condition) throws NamingException;

    /**
     * Быстрый фильтр для значений одного поля
     *
     * @param valueName
     * @param a
     * @param b
     * @return
     * @throws hep.dataforge.exceptions.NamingException
     */
    default PointSet filter(String valueName, Value a, Value b) throws NamingException {
        return this.filter(getValueCondition(valueName, a, b));
    }

    default PointSet filter(String valueName, double a, double b) throws NamingException {
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
    default PointSet filter(String... tags) throws NamingException {
        return this.filter(getTagCondition(tags));
    }

    DataPoint get(int i);
    Column getColumn(String name) throws NameNotFoundException;

    Format getDataFormat();

    Value getValue(int index, String name) throws NameNotFoundException;

    int size();

    PointSet sort(String name, boolean ascending);

    PointSet sort(Comparator<DataPoint> comparator);
    
    Stream<DataPoint> stream();

    void setMeta(Meta meta);
}

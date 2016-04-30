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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import static hep.dataforge.tables.Filtering.getTagCondition;
import static hep.dataforge.tables.Filtering.getValueCondition;
import hep.dataforge.values.Value;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An immutable table of values
 * @author Alexander Nozik
 */
public interface Table extends PointSource {

    DataPoint getRow(int i);

    Column getColumn(String name) throws NameNotFoundException;

    default Value getValue(int index, String name) throws NameNotFoundException{
        return getRow(index).getValue(name);
    }

    int size();
    
    Table transform(UnaryOperator<Stream<DataPoint>> streamTransform);

    default Stream<DataPoint> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
    
    default Table sort(Comparator<DataPoint> comparator) {
        return transform(stream -> stream.sorted(comparator));
    }    

    default Table sort(String name, boolean ascending) {
        return transform(stream -> stream.sorted((DataPoint o1, DataPoint o2) -> {
            int signum = ascending ? +1 : -1;
            return o1.getValue(name).compareTo(o2.getValue(name)) * signum;
        }));
    }
    /**
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     *
     * @param condition a {@link java.util.function.Predicate} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     * @return a {@link hep.dataforge.tables.Table} object.
     */
    default Table filter(Predicate<DataPoint> condition) throws NamingException{
        return transform(stream -> stream.filter(condition));
    }

    /**
     * Быстрый фильтр для значений одного поля
     *
     * @param valueName
     * @param a
     * @param b
     * @return
     * @throws hep.dataforge.exceptions.NamingException
     */
    default Table filter(String valueName, Value a, Value b) throws NamingException {
        return this.filter(getValueCondition(valueName, a, b));
    }

    default Table filter(String valueName, double a, double b) throws NamingException {
        return this.filter(getValueCondition(valueName, Value.of(a), Value.of(b)));
    }    
    
    /**
     * Быстрый фильтр по меткам
     *
     * @param tags
     * @throws hep.dataforge.exceptions.NamingException
     * @return a {@link hep.dataforge.tables.Column} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    default Table filter(String... tags) throws NamingException {
        return this.filter(getTagCondition(tags));
    }    
}

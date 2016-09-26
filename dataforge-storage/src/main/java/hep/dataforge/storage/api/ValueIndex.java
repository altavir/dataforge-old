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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An index that uses a Value corresponding to each indexed element
 *
 * @author Alexander Nozik
 */
public interface ValueIndex<T> extends Index<T> {

    /**
     * Search for the index field value closest to provided one. Specific search
     * mechanism could differ for different loaders.
     *
     * @param value
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    List<T> pull(Value value) throws StorageException;

    default List<T> pull(Object value) throws StorageException {
        return pull(Value.of(value));
    }

    /**
     * Pull the first entry with given key
     *
     * @param value
     * @return
     * @throws StorageException
     */
    default T pullOne(Value value) throws StorageException {
        List<T> res = pull(value);
        if (!res.isEmpty()) {
            return res.get(0);
        } else {
            return null;
        }
    }

    /**
     * Возвращает список точек, ключ которых лежит строго в пределах от from до
     * to. Работает только для сравнимых значений (для строк может выдавать
     * ерунду)
     *
     * @param from
     * @param to
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    default List<T> pull(Value from, Value to) throws StorageException {
        return pull(from, to, -1);
    }

    default List<T> pull(Object from, Object to) throws StorageException {
        return pull(Value.of(from), Value.of(to));
    }

    /**
     * Возвращает список точек, ключ которых лежит строго в пределах от from до
     * to. В случае если число точек в диапазоне превышает {@code maxItems},
     * выкидывает не все точки, а точки с некоторым шагом. Методика фильтрации
     * специфична за загрузчика.
     *
     * @param from
     * @param to
     * @param maxItems
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    List<T> pull(Value from, Value to, int maxItems) throws StorageException;

    default List<T> pull(Object from, Object to, int maxItems) throws StorageException {
        return pull(Value.of(from), Value.of(to), maxItems);
    }

    @Override
    default Stream<Supplier<T>> query(Meta query) throws StorageException {
        Value from = query.getValue("from", Value.NULL);
        Value to = query.getValue("to", Value.NULL);
        int limit = query.getInt("limit", -1);
        return pull(from, to, limit).stream().<Supplier<T>>map((T t) -> () -> t);
    }

}

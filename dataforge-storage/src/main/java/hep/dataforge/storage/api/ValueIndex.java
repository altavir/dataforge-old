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
import hep.dataforge.storage.commons.StorageUtils;
import hep.dataforge.values.Value;

import java.util.List;
import java.util.function.Supplier;

/**
 * An index that uses a Value corresponding to each indexed element
 *
 * @author Alexander Nozik
 */
public interface ValueIndex<T> extends Index<T> {

    String FROM_KEY = "from";
    String TO_KEY = "to";
    String LIMIT_KEY = "limit";

    /**
     * Search for the index field value closest to provided one. Specific search
     * mechanism could differ for different loaders.
     *
     * @param value
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    List<Supplier<T>> pull(Value value) throws StorageException;

    default List<Supplier<T>> pull(Object value) throws StorageException {
        return pull(Value.of(value));
    }

    /**
     * Pull the first entry with given key
     *
     * @param value
     * @return
     * @throws StorageException
     */
    default Supplier<T> pullOne(Value value) throws StorageException {
        List<Supplier<T>> res = pull(value);
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
    List<Supplier<T>> pull(Value from, Value to) throws StorageException;

    default List<Supplier<T>> pull(Object from, Object to) throws StorageException {
        return pull(Value.of(from), Value.of(to));
    }

    /**
     * By default uses smart optimized index pull
     * @param query
     * @return
     * @throws StorageException
     */
    @Override
    default List<Supplier<T>> query(Meta query) throws StorageException {
        //TODO add support for query engines
        //null values correspond to
        Value from = query.getValue(FROM_KEY, Value.NULL);
        Value to = query.getValue(TO_KEY, Value.NULL);
        if (query.hasValue(LIMIT_KEY)) {
            return StorageUtils.pullFiltered(this, from, to, query.getInt(LIMIT_KEY));
        } else {
            return pull(from, to);
        }
    }

}

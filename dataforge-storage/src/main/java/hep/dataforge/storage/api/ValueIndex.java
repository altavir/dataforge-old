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

import java.util.NavigableSet;
import java.util.Optional;
import java.util.stream.Stream;

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
     * mechanism could differ for different indexes.
     *
     * @param value
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    Stream<T> pull(Value value) throws StorageException;

    default Stream<T> pull(Object value) throws StorageException {
        return pull(Value.of(value));
    }

    /**
     * Pull the first entry with given key
     *
     * @param value
     * @return
     * @throws StorageException
     */
    default Optional<T> pullOne(Value value) throws StorageException {
        return pull(value).findFirst();
    }

    default Optional<T> pullOne(Object value) throws StorageException {
        return pullOne(Value.of(value));
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
    Stream<T> pull(Value from, Value to) throws StorageException;

    default Stream<T> pull(Object from, Object to) throws StorageException {
        return pull(Value.of(from), Value.of(to));
    }

    /**
     * A sparse pull operation with limited number of results.
     * This method does not guarantee specific node placement but tries to place them as uniformly as possible.
     * It is intended primarily for visualization.
     *
     * @param from
     * @param to
     * @param limit
     * @return
     * @throws StorageException
     */
    default Stream<T> pull(Value from, Value to, int limit) throws StorageException {
        return StorageUtils.sparsePull(this, from, to, limit);
    }

    /**
     * By default uses smart optimized index pull
     *
     * @param query
     * @return
     * @throws StorageException
     */
    @Override
    default Stream<T> query(Meta query) throws StorageException {
        //TODO add support for query engines
        //null values correspond to
        Value from = query.getValue(FROM_KEY, Value.NULL);
        Value to = query.getValue(TO_KEY, Value.NULL);
        if (query.hasValue(LIMIT_KEY)) {
            return pull(from, to, query.getInt(LIMIT_KEY));
        } else {
            return pull(from, to);
        }
    }

    NavigableSet<Value> keySet() throws StorageException;

    default Value getFirstKey() throws StorageException {
        return keySet().first();
    }

    default Value getLastKey() throws StorageException {
        return keySet().last();
    }


}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.StorageQueryException;
import hep.dataforge.values.Value;
import java.util.List;
import hep.dataforge.data.PointSet;

/**
 * Some indexed reader. Each index pull operation should be run in a separate
 * process
 *
 * @author Alexander Nozik
 */
public interface Index<T> {

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
     * @param value
     * @return
     * @throws StorageException 
     */
    default T pullOne(Value value) throws StorageException{
        List<T> res = pull(value);
        if(!res.isEmpty()){
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
    List<T> pull(Value from, Value to) throws StorageException;

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
    
    /**
     * Pull a number of points according to given Query. If query is supported
     * but no matching results found, empty list is returned. The results are
     * supposed to be ordered, but it is not guaranteed.
     *
     * @param query
     * @return
     * @throws StorageQueryException
     */
    default PointSet pull(Query query) throws StorageException{
        throw new StorageException("Query not supported");
    }
}

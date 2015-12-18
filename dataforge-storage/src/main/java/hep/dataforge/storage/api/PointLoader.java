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
package hep.dataforge.storage.api;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.PointListener;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.StorageQueryException;
import hep.dataforge.values.Value;
import java.util.Collection;

/**
 * PointLoader is intended to load a set of datapoints. The loader can have one
 * index field by which it could be sorted and searched. If index field is not
 * defined, than default internal indexing mechanism is used.
 *
 * @author Darksnake
 */
@NodeDef(name = "format", required = true, info = "data point format for this loader")
@ValueDef(name = "index", def = "", info = "The name of index field for this loader")
public interface PointLoader extends Loader {

    public static final String POINT_LOADER_TYPE = "point";
    public static final String DEFAULT_INDEX_FIELD = "";

    public static final String LOADER_FORMAT_KEY = "format";

    /**
     * Pull the whole loader as a data set.
     *
     * @return
     * @throws StorageException
     */
    DataSet asDataSet() throws StorageException;

    /**
     * Search for the index field value closest to provided one. Specific search
     * mechanism could differ for different loaders.
     *
     * @param value
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    DataPoint pull(Value value) throws StorageException;

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
    default DataSet pull(Value from, Value to) throws StorageException {
        return pull(from, to, Integer.MAX_VALUE);
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
    DataSet pull(Value from, Value to, int maxItems) throws StorageException;

    /**
     * Push the DataPoint to the loader.
     *
     * @param dp
     */
    void push(DataPoint dp) throws StorageException;

    /**
     * Push a collection of DataPoints. This method should be overridden when
     * Loader commit operation is expensive and should be used once for the
     * whole collection.
     *
     * @param dps
     * @throws StorageException
     */
    void push(Collection<DataPoint> dps) throws StorageException;

    /**
     * Pull a number of points according to given Query. If query is supported
     * but no matching results found, empty list is returned. The results are
     * supposed to be ordered, but it is not guaranteed.
     *
     * @param query
     * @return
     * @throws StorageQueryException
     */
    DataSet pull(Query query) throws StorageException;

    /**
     * The name of main index field
     * @return 
     */
    default String indexField() {
        return meta().getString("index", DEFAULT_INDEX_FIELD);
    }
    
    /**
     * Set a PointListener which is called on each push operations
     * @param listener 
     */
    void addPointListener(PointListener listener);
    
    /**
     * Remove current PointLostener. If no PointListener is registered, do nothing.
     */
    void removePointListener(PointListener listener);

}

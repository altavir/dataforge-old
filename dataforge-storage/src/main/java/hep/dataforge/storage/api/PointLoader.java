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

import hep.dataforge.description.NodeDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.commons.ValueIndex;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointListener;
import hep.dataforge.tables.PointSource;
import hep.dataforge.tables.TableFormat;
import java.util.Collection;

/**
 * PointLoader is intended to load a set of datapoints. The loader can have one
 * index field by which it could be sorted and searched. If index field is not
 * defined, than default internal indexing mechanism is used.
 *
 * @author Darksnake
 */
@NodeDef(name = "format", required = true, info = "data point format for this loader")
//@ValueDef(name = "defaultIndexName", def = "timestamp", info = "The name of index field for this loader")
public interface PointLoader extends Loader, PointSource {

    public static final String POINT_LOADER_TYPE = "point";
    public static final String DEFAULT_INDEX_FIELD = "";

    public static final String LOADER_FORMAT_KEY = "format";

//    /**
//     * Pull the whole loader as a data set.
//     *
//     * @return
//     * @throws StorageException
//     */
//    Table asTable() throws StorageException;
    
    /**
     * The minimal format for points in this loader. Is null for unformatted loader
     * @return 
     */
    @Override
    TableFormat getFormat();

//    /**
//     * Build a custom index. In case it is a map index it could be stored
//     * somewhere.
//     *
//     * @param indexMeta
//     * @return
//     */
//    Index<DataPoint> buildIndex(Meta indexMeta);

    /**
     * Get index for given value name. If name is null or empty, default point
     * number index is returned. This operation chooses the fastest existing
     * index or creates new one (if index is created than it is optimized for
     * single operation performance).
     *
     * @param name
     * @return
     */
    ValueIndex<DataPoint> getIndex(String name);

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
     * Set a PointListener which is called on each push operations
     *
     * @param listener
     */
    void addPointListener(PointListener listener);

    /**
     * Remove current PointLostener. If no PointListener is registered, do
     * nothing.
     */
    void removePointListener(PointListener listener);

}

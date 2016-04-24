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
package hep.dataforge.storage.loaders;

import hep.dataforge.points.DataPoint;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.points.PointFormat;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.StorageUtils;
import java.util.Iterator;
import org.slf4j.LoggerFactory;
import hep.dataforge.storage.commons.ValueIndex;

/**
 * The point loader that mirrors all points to the secondary storage
 *
 * @author Alexander Nozik
 */
public class ChainPointLoader extends AbstractPointLoader {

    private final PointLoader primaryLoader;
    private final PointLoader secondaryLoader;
    
    /**
     * Build a chain point loader with given meta. Meta is required to be point loader meta and both storages should support point loaders
     * @param loaderMeta
     * @param primaryStorage
     * @param secondaryStorage
     * @return
     * @throws StorageException 
     */
    public static ChainPointLoader build(Meta loaderMeta, Storage primaryStorage, Storage secondaryStorage) throws StorageException{
        if(!StorageUtils.loaderType(loaderMeta).startsWith(PointLoader.POINT_LOADER_TYPE)){
            throw new RuntimeException("ChainPointLoader requires point loader meta");
        }
        
        return new ChainPointLoader((PointLoader)primaryStorage.buildLoader(loaderMeta), (PointLoader)secondaryStorage.buildLoader(loaderMeta));
    }

    public ChainPointLoader(PointLoader primaryPointLoader, PointLoader secondaryPointLoader) {
        super(primaryPointLoader.getStorage(), primaryPointLoader.getName(), primaryPointLoader.meta());
        this.primaryLoader = primaryPointLoader;
        this.secondaryLoader = secondaryPointLoader;
    }

    @Override
    public boolean isEmpty() {
        return primaryLoader.isEmpty();
    }

    @Override
    public PointFormat getFormat() {
        return primaryLoader.getFormat();
    }

    
    
//    @Override
//    public PointSet asPointSet() throws StorageException {
//        return primaryLoader.asPointSet();
//    }

    @Override
    public ValueIndex<DataPoint> getIndex(String name) {
        return primaryLoader.getIndex(name);
    }

//    @Override
//    public Index<DataPoint> buildIndex(Meta indexMeta) {
//        return primaryLoader.buildIndex(indexMeta);
//    }

    @Override
    public Iterator<DataPoint> iterator() {
        return primaryLoader.iterator();
    }

    @Override
    public void pushPoint(DataPoint dp) throws StorageException {
        primaryLoader.push(dp);
        try {
            secondaryLoader.push(dp);
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Can't write point to the backup storage", ex);
        }
    }

    @Override
    public void open() throws Exception {
        primaryLoader.open();
        secondaryLoader.open();
    }

    @Override
    public void close() throws Exception {
        primaryLoader.close();
        secondaryLoader.close();
    }

}

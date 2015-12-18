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
package hep.dataforge.storage.oak;

import hep.dataforge.annotations.Annotation;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.common.AbstractStorage;

/**
 *
 * @author Alexander Nozik
 */
public class OAKStorage extends AbstractStorage {

    public OAKStorage(Storage parent, String name, Annotation annotation) {
        super(parent, name, annotation);
    }

    @Override
    public Loader buildLoader(Annotation loaderConfiguration) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Storage buildShelf(String shelfName, Annotation shelfConfiguration) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

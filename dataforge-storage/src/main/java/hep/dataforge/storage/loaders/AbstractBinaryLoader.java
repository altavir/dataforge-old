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

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.ObjectLoader;
import hep.dataforge.storage.api.Storage;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractBinaryLoader<T> extends AbstractLoader implements ObjectLoader<T> {

    public AbstractBinaryLoader(Storage storage, String name, Meta meta) {
        super(storage, name, meta);
    }


    @Override
    public Envelope respond(Envelope message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String getType() {
        return OBJECT_LOADER_TYPE;
    }
    
    
    
}

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
package hep.dataforge.storage.commons;

import hep.dataforge.meta.Meta;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.loaders.AbstractLoader;

/**
 * Пустой сервер. Вместо записи в файл, кидает на консоль
 *
 * @author Darksnake
 */
public class DummyStorage extends AbstractStorage {

    public DummyStorage(String name) {
        super(name);
    }

    public DummyStorage() {
        super(null);
    }

    @Override
    public Loader buildLoader(Meta loaderConfig) throws StorageException {
        return new DummyLoader(this);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Storage buildShelf(String path, Meta an) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class DummyLoader extends AbstractLoader {

        public DummyLoader(Storage storage) {
            super(storage, "dummy", null);
        }

        @Override
        public Envelope respond(Envelope message) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void open() throws Exception {

        }
    }

}

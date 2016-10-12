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
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;

/**
 *
 * @author darksnake
 */
public class BasicMaskLoader implements Loader {

    private final Loader loader;

    public BasicMaskLoader(Loader loader) {
        this.loader = loader;
    }
    
    @Override
    public void close() throws Exception {
        loader.close();
    }

    @Override
    public Meta meta() {
        return loader.meta();
    }

    @Override
    public String getDescription() {
        return loader.getDescription();
    }

    @Override
    public String getName() {
        return loader.getName();
    }

    @Override
    public Storage getStorage() {
        return loader.getStorage();
    }

    @Override
    public String getType() {
        return loader.getType();
    }

    @Override
    public boolean isReadOnly() {
        return loader.isReadOnly();
    }

    @Override
    public boolean isOpen() {
        return loader.isOpen();
    }

    @Override
    public MessageValidator getValidator() {
        return loader.getValidator();
    }

    @Override
    public void open() throws Exception {
        loader.open();
    }

    @Override
    public Envelope respond(Envelope message) {
        return loader.respond(message);
    }

    @Override
    public boolean isEmpty() {
        return loader.isEmpty();
    }
}

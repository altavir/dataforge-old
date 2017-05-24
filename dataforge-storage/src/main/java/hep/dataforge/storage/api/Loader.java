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

import hep.dataforge.control.AutoConnectible;
import hep.dataforge.control.RoleDef;
import hep.dataforge.events.EventHandler;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Path;

/**
 * A typed loader.
 *
 *
 * @author Alexander Nozik
 */
@RoleDef(name = "eventListener", objectType = EventHandler.class, info = "Handle events produced by this loader")
public interface Loader extends Metoid, AutoCloseable, Named, Responder, AutoConnectible {

    String LOADER_NAME_KEY = "name";
    String LOADER_TYPE_KEY = "type";

    @Override
    void close() throws Exception;

    /**
     * The loader description
     *
     * @return
     */
    String getDescription();

    /**
     * Storage, которому соответствует этот загрузчик. В случае, если загрузчик
     * существует отдельно от сервера, возвращается null
     *
     * @return
     */
    Storage getStorage();

    String getType();

    boolean isReadOnly();
    
    boolean isOpen();

    void open() throws Exception;

    boolean isEmpty();

    MessageValidator getValidator();

    /**
     * Get full path to this loader relative to root storage
     *
     * @return
     */
    default String getPath() {
        return getStorage().getFullPath() + Path.PATH_SEGMENT_SEPARATOR + getName();
    }
    //TODO add getRelativePath method
}

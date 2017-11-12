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

import hep.dataforge.context.Context;
import hep.dataforge.context.ContextAware;
import hep.dataforge.control.AutoConnectible;
import hep.dataforge.control.RoleDef;
import hep.dataforge.events.EventHandler;
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.AlphanumComparator;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static hep.dataforge.control.Connection.EVENT_HANDLER_ROLE;
import static hep.dataforge.control.Connection.LOGGER_ROLE;

/**
 * A typed loader.
 *
 * @author Alexander Nozik
 */
@RoleDef(name = EVENT_HANDLER_ROLE, objectType = EventHandler.class, info = "Handle events produced by this loader")
@RoleDef(name = LOGGER_ROLE, objectType = Logger.class, unique = true, info = "The logger for this loader")
public interface Loader extends Metoid, AutoCloseable, Named, Responder, AutoConnectible, ContextAware, Comparable<Named> {

    String LOADER_NAME_KEY = "name";
    String LOADER_TYPE_KEY = "type";

    @Override
    void close() throws Exception;

    /**
     * The loader description
     *
     * @return
     */
    default String getDescription() {
        return meta().getString("description", "");
    }

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
    default Path getPath() {
        return Path.of("", getStorage().getFullName()).append(Path.of("", Name.ofSingle(getName())));
    }

    default Name getFullName() {
        return getStorage().getFullName().append(getName());
    }

    /**
     * Get full meta including storage layers
     *
     * @return
     */
    default Laminate getLaminate() {
        return getStorage().getLaminate().withFirstLayer(meta());
    }

    @Override
    default Context getContext() {
        return getStorage().getContext();
    }

    @Override
    default int compareTo(@NotNull Named o) {
        return AlphanumComparator.INSTANCE.compare(this.getName(), o.getName());
    }

    @Override
    default Logger getLogger() {
        return optConnection(LOGGER_ROLE, Logger.class).orElse(getContext().getLogger());
    }
}

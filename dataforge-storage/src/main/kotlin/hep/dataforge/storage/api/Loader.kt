/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.storage.api

import hep.dataforge.Named
import hep.dataforge.connections.AutoConnectible
import hep.dataforge.connections.Connection.EVENT_HANDLER_ROLE
import hep.dataforge.connections.Connection.LOGGER_ROLE
import hep.dataforge.connections.RoleDef
import hep.dataforge.connections.RoleDefs
import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.description.ValueDef
import hep.dataforge.events.EventHandler
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.messages.Responder
import hep.dataforge.io.messages.Validator
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Metoid
import hep.dataforge.names.AlphanumComparator
import hep.dataforge.names.Name
import hep.dataforge.providers.Path
import hep.dataforge.storage.api.Loader.Companion.LOADER_TYPE_KEY
import org.slf4j.Logger

/**
 * A typed loader.
 *
 * @author Alexander Nozik
 */
@RoleDefs(
        RoleDef(name = EVENT_HANDLER_ROLE, objectType = EventHandler::class, info = "Handle events produced by this loader"),
        RoleDef(name = LOGGER_ROLE, objectType = Logger::class, unique = true, info = "The logger for this loader")
)
@ValueDef(key = LOADER_TYPE_KEY, info = "The type of the loader")
interface Loader : Metoid, AutoCloseable, Named, Responder, AutoConnectible, ContextAware, Comparable<Named> {

    /**
     * The loader description
     *
     * @return
     */
    val description: String
        get() = meta.getString("description", "")

    /**
     * Storage, которому соответствует этот загрузчик. В случае, если загрузчик
     * существует отдельно от сервера, возвращается null
     *
     * @return
     */
    val storage: Storage

    val type: String

    val isReadOnly: Boolean

    val isOpen: Boolean

    val isEmpty: Boolean

    val validator: Validator

    /**
     * Get full path to this loader relative to root storage
     *
     * @return
     */
    val path: Path
        get() = Path.of("", storage.fullName).append(Path.of("", Name.ofSingle(name)))

    val fullName: Name
        get() = storage.fullName.append(name)

    /**
     * Get full meta including storage layers
     *
     * @return
     */
    val laminate: Laminate
        get() = storage.laminate.withFirstLayer(meta)

    @Throws(Exception::class)
    override fun close()

    override val context: Context
        get() = storage.context

    override fun compareTo(other: Named): Int {
        return AlphanumComparator.INSTANCE.compare(this.name, other.name)
    }

    override val logger: Logger
        get() = optConnection(LOGGER_ROLE, Logger::class.java).orElse(context.logger)

    companion object {
        //        const val LOADER_NAME_KEY = "name"
        const val LOADER_TYPE_KEY = Envelope.ENVELOPE_DATA_TYPE_KEY
    }
}

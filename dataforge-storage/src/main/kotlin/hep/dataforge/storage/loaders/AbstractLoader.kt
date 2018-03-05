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
package hep.dataforge.storage.loaders

import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.events.Event
import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.PushFailedException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.messages.Validator
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Loader
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.commons.StorageUtils

/**
 * @author Alexander Nozik
 */
abstract class AbstractLoader(final override val storage: Storage, private val name: String, private val meta: Meta) : Loader {
    private val connectionHelper = ConnectionHelper(this, storage.logger)

    override val validator: Validator = StorageUtils.defaultMessageValidator(Storage.LOADER_TARGET, name)


    override fun getConnectionHelper(): ConnectionHelper {
        return connectionHelper
    }

    override fun getMeta(): Meta {
        return meta
    }

    override fun getName(): String {
        return name
    }

    /**
     * Loader meta must be set here if it is not set by constructor
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    abstract override fun open()

    @Throws(PushFailedException::class)
    protected fun tryPush() {
        if (isReadOnly) {
            throw PushFailedException(this, "Trying to push to read only loader.")
        }
    }

    /**
     * Notify all connections which can handle events
     * @param event
     */
    protected fun dispatchEvent(event: Event) {
        forEachConnection(EventHandler::class.java) { eventHandler -> eventHandler.pushEvent(event) }
    }

    protected fun checkOpen() {
        if (!isOpen) {
            try {
                open()
            } catch (ex: Exception) {
                throw RuntimeException("Can't open loader", ex)
            }

        }
    }

    override fun respond(message: Envelope): Envelope {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

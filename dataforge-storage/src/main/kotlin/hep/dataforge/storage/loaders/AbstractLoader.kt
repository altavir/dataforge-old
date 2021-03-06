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
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Loader
import hep.dataforge.storage.api.Storage

/**
 * @author Alexander Nozik
 */
abstract class AbstractLoader(final override val storage: Storage, final override val name: String, override val meta: Meta) : Loader {
    private val _connectionHelper by lazy{ ConnectionHelper(this)}

//    override val validator: Validator = StorageUtils.defaultMessageValidator(Storage.LOADER_TARGET, name)


    override fun getConnectionHelper(): ConnectionHelper {
        return _connectionHelper
    }

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

}

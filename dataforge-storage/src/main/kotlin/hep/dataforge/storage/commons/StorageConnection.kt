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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons

import hep.dataforge.connections.Connectible
import hep.dataforge.connections.Connection
import hep.dataforge.connections.ConnectionFactory
import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.exceptions.NotConnectedException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.messages.Responder
import hep.dataforge.io.messages.errorResponseBase
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.names.AnonymousNotAlowed
import hep.dataforge.storage.api.Storage

/**
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
class StorageConnection : Connection, Responder, Metoid, ContextAware {

    private val meta: Meta
    val storage: Storage

    /**
     * Connection to predefined storage
     *
     * @param storage
     */
    constructor(storage: Storage) {
        this.storage = storage
        this.meta = storage.meta
    }

    /**
     * Create storage from context and meta
     */
    constructor(context: Context, meta: Meta) {
        this.meta = meta
        val storageManager = context.pluginManager.load(StorageManager::class.java)
        this.storage = storageManager.buildStorage(meta)
    }

    override fun isOpen(): Boolean {
        return storage.isOpen
    }

    @Throws(Exception::class)
    override fun open(obj: Any) {
        storage.open()
    }

    override fun respond(message: Envelope): Envelope {
        return if (isOpen) {
            storage.respond(message)
        } else {
            errorResponseBase(message, NotConnectedException(this)).build()
        }
    }

    @Throws(Exception::class)
    override fun close() {
        if (isOpen) {
            storage.close()
        }
    }

    override fun getContext(): Context {
        return storage.context
    }

    override fun getMeta(): Meta {
        return meta
    }

    class Factory : ConnectionFactory {

        override fun getType(): String {
            return "df.storage"
        }

        override fun <T : Connectible> build(obj: T, context: Context, meta: Meta): Connection {
            return if (obj is Metoid) {
                StorageConnection(context,
                        Laminate((obj as Metoid).meta.getMetaOrEmpty("storage"), meta)
                )
            } else {
                StorageConnection(context, meta)
            }
        }
    }
}

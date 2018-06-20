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
import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Meta
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.storage.api.MetaStateChangedEvent
import hep.dataforge.storage.api.StateChangedEvent
import hep.dataforge.values.Value
import java.util.stream.Stream

/**
 * A helper class to store states
 */
class StateHolder(val connections: ConnectionHelper, val handler: MetaHandler) :
        SimpleConfigurable(handler.pull() ?: Meta.empty()) {


//    override fun respond(message: Envelope): Envelope {
//        update()
//        try {
//            val envelopeMeta = message.meta
//            val res = responseBase(message)
//            when (envelopeMeta.getString(ACTION_KEY)) {
//                PUSH_ACTION -> {
//                    envelopeMeta.useEachMeta("state") {
//                        if (it.hasValue("value")) {
//                            push(it.getString("key"), it.getValue("value"))
//                        } else if (it.hasMeta("value")) {
//                            push(it.getString("key"), it.getMeta("value"))
//                        }
//                    }
//                    //TODO add expanded response?
//                    res.setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_OK)
//                    return res.build()
//                }
//                PULL_ACTION -> {
//                    if (envelopeMeta.hasValue("state") || envelopeMeta.hasValue("metaState")) {
//                        envelopeMeta.useValue("state"){
//                            it.list.forEach {
//                                res.putMetaNode(buildMeta("state", "key" to it, "value" to config.getValue(it.string)))
//                            }
//                        }
//                        envelopeMeta.useValue("metaState"){
//                            it.list.forEach {
//                                res.putMetaNode(buildMeta("state", "key" to it){
//                                    putNode("value", config.getMeta(it.string))
//                                })
//                            }
//
//                        }
//                    } else {
//                        states.forEach {
//                            res.putMetaNode(buildMeta("state", "key" to it.first, "value" to it.second))
//                        }
//                        metaStates.forEach {
//                            res.putMetaNode(buildMeta("state", "key" to it.first) {
//                                putNode("value", it.second)
//                            })
//                        }
//                    }
//                    res.setMetaValue(MESSAGE_STATUS_KEY, MESSAGE_OK)
//                    return res.build()
//                }
//
//                else -> throw NotDefinedException("Unknown action")
//            }
//
//        } catch (ex: Exception) {
//            return StorageMessageUtils.exceptionResponse(message, ex)
//        }
//
//    }

    val states: Stream<Pair<String, Value>>
        get() = config.valueNames.map { Pair(it, config.getValue(it)) }

    val metaStates: Stream<Pair<String, Meta>>
        get() = config.nodeNames.map { Pair(it, config.getMeta(it)) }

    @Throws(StorageException::class)
    fun push(path: String, value: Any) {
        update()
        if (value is Meta) configureNode(path, value)
        else configureValue(path, value)
    }

    override fun applyValueChange(name: String, oldValue: Value?, newValue: Value?) {
        super.applyValueChange(name, oldValue, newValue)
        val event = StateChangedEvent.build(name, oldValue ?: Value.NULL, newValue ?: Value.NULL)
        connections.forEachConnection(EventHandler::class.java) { it.pushEvent(event) }
    }

    override fun applyNodeChange(name: String, oldValue: MutableList<out Meta>, newValue: MutableList<out Meta>) {
        super.applyNodeChange(name, oldValue, newValue)
        val event = MetaStateChangedEvent.build(
                name,
                oldValue.firstOrNull() ?: Meta.empty(),
                newValue.firstOrNull() ?: Meta.empty()
        )
        connections.forEachConnection(EventHandler::class.java) { it.pushEvent(event) }
    }

    interface MetaHandler {
        val hash: Int

        fun push(meta: Meta)
        fun pull(): Meta?
    }

    /**
     * Check if loader is upToDate and update it if necessarily
     */
    private fun update() {
        if (handler.hash != config.hashCode()) {
            //update is ignored if meta is null
            this.config.update(handler.pull())
        }
    }

}
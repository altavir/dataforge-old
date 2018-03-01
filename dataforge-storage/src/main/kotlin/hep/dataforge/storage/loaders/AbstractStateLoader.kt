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

import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.NotDefinedException
import hep.dataforge.exceptions.StorageException
import hep.dataforge.exceptions.WrongTargetException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.storage.api.StateChangedEvent
import hep.dataforge.storage.api.StateLoader
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.commons.MessageFactory
import hep.dataforge.storage.commons.StorageMessageUtils
import hep.dataforge.utils.Optionals
import hep.dataforge.values.Value
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author darksnake
 */
abstract class AbstractStateLoader(storage: Storage, name: String, annotation: Meta) : AbstractLoader(storage, name, annotation), StateLoader {

    protected val states: MutableMap<String, Value> = ConcurrentHashMap()
    protected var defaults: MutableMap<String, Value> = HashMap()
    protected var isUpToDate = false

    override val isEmpty: Boolean
        get() {
            check()
            return states.isEmpty()
        }

    override val type: String
        get() = StateLoader.STATE_LOADER_TYPE

    override val stateNames: Set<String>
        get() {
            check()
            return states.keys
        }

    /**
     * the default values are not stored in the database and should be defined
     * in program
     *
     * @param name
     * @param value
     */
    protected fun setDefaultValue(name: String, value: Value) {
        this.defaults[name] = value
    }

    protected fun setDefaults(defaults: MutableMap<String, Value>) {
        this.defaults = defaults
    }

    override fun optValue(name: String): Optional<Value> {
        check()
        return Optionals.either(Optional.ofNullable(states[name]))
                .or { Optional.ofNullable(defaults[name]) }
                .opt()
    }

    override fun hasValue(name: String): Boolean {
        check()
        return states.containsKey(name) || defaults.containsKey(name)
    }

    override fun respond(message: Envelope): Envelope {
        check()
        try {
            if (!validator.isValid(message)) {
                return StorageMessageUtils.exceptionResponse(message, WrongTargetException())
            }
            val envelopeMeta = message.meta
            val operation = envelopeMeta.getString(ACTION_KEY)
            val res = MessageFactory().responseBase(message)
            when (operation) {
                PUSH_OPERATION, "set" -> {
                    if (envelopeMeta.hasMeta("state")) {
                        for (state in envelopeMeta.getMetaList("state")) {
                            val stateName = state.getString("name")
                            val stateValue = state.getString("value")
                            pushState(stateName, stateValue)
                            res.putMetaNode(MetaBuilder("state")
                                    .putValue("name", stateName)
                                    .putValue("value", stateValue))
                        }
                    } else if (envelopeMeta.hasValue("state")) {
                        val stateName = envelopeMeta.getString("name")
                        val stateValue = envelopeMeta.getString("value")
                        pushState(stateName, stateValue)
                        res.putMetaNode(MetaBuilder("state")
                                .putValue("name", stateName)
                                .putValue("value", stateValue))
                    }

                    return res.build()
                }
                PULL_OPERATION, "get" -> {
                    val names: Array<String>
                    if (envelopeMeta.hasValue("name")) {
                        names = envelopeMeta.getStringArray("name")
                    } else {
                        names = stateNames.toTypedArray()
                    }
                    for (stateName in names) {
                        if (hasValue(stateName)) {
                            val stateValue = getString(stateName)
                            res.putMetaNode(MetaBuilder("state")
                                    .putValue("name", stateName)
                                    .putValue("value", stateValue))
                        }
                    }

                    return res.build()
                }

                else -> throw NotDefinedException("Unknown operation")
            }

        } catch (ex: StorageException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        } catch (ex: UnsupportedOperationException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        } catch (ex: NotDefinedException) {
            return StorageMessageUtils.exceptionResponse(message, ex)
        }

    }

    @Throws(StorageException::class)
    override fun pushState(name: String, value: Value) {
        check()
        var oldValue: Value? = states[name]
        if (oldValue == null) {
            oldValue = Value.getNull()
        }
        states[name] = value
        commit()
        val event = StateChangedEvent.build(name, oldValue, value)
        forEachConnection("eventListener", EventHandler::class.java) { handler -> handler.pushEvent(event) }
    }

    @Throws(StorageException::class)
    protected abstract fun commit()

    @Throws(StorageException::class)
    protected abstract fun update()

    /**
     * Check if loader is upToDate and update it if necessarily
     */
    private fun check() {
        if (!isUpToDate) {
            try {
                LoggerFactory.getLogger(javaClass).debug("Bringing state loader up to date")
                update()
            } catch (ex: StorageException) {
                throw RuntimeException("Can't update state loader")
            }

        }
    }

}

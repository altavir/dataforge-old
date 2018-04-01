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

package hep.dataforge.states

import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.kodex.listAnnotations
import hep.dataforge.kodex.optional
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaMorph
import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass


/**
 * An object that could have a set of readonly or read/write states
 */
interface Stateful : Provider {

    val states: StateHolder

    @Provides(STATE_TARGET)
    fun optState(stateName: String): State<*>? {
        return states[stateName]
    }

    @get:ProvidesNames(STATE_TARGET)
    val stateNames: Collection<String>
        get() = states.names

    companion object {
        const val STATE_TARGET = "state"
    }
}

/**
 * Create a new meta state using class MetaState annotation if it is present and register it
 */
fun Stateful.metaState(
        name: String,
        getter: (suspend () -> Meta)? = null,
        setter: (suspend (Meta?, Meta) -> Meta?)? = null
): MetaState {
    val def: MetaStateDef? = listAnnotations(this::class.java, MetaStateDef::class.java, true).find { it.value.name == name }
    val state: MetaState = if (def == null) {
        MetaState(name = name, getter = getter, setter = setter)
    } else {
        MetaState(def.value, getter, setter)
    }
    states.init(state)
    return state
}

fun Stateful.valueState(
        name: String,
        getter: (suspend () -> Any)? = null,
        setter: (suspend (Value?, Value) -> Any?)? = null
): ValueState {
    val def: StateDef? = listAnnotations(this::class.java, StateDef::class.java, true).find { it.value.name == name }
    val state: ValueState = if (def == null) {
        ValueState(name = name, getter = getter, setter = setter)
    } else {
        ValueState(def.value, getter, setter)
    }
    states.init(state)
    return state
}

fun <T: MetaMorph> Stateful.morphState(
        name: String,
        type: KClass<T>,
        def: T? = null,
        getter: (suspend () -> T)? = null,
        setter: (suspend (T?, T) -> T?)? = null
): MorphState<T>{
    val state = MorphState<T>(name, type, def, getter, setter)
    states.init(state)
    return state
}

class StateHolder(val logger: Logger = LoggerFactory.getLogger(StateHolder::class.java)) : Provider, Iterable<State<*>>, ValueProvider {
    private val stateMap: MutableMap<String, State<*>> = HashMap()

    operator fun get(stateName: String): State<*>? {
        return stateMap[stateName]
    }

    /**
     * null invalidates the state
     */
    operator fun set(stateName: String, value: Any?) {
        this[stateName]?.set(value) ?: throw NameNotFoundException(stateName)
    }

    val names: Collection<String>
        get() = stateMap.keys

    fun stream(): Stream<State<*>> {
        return stateMap.values.stream()
    }

    override fun iterator(): Iterator<State<*>> {
        return stateMap.values.iterator()
    }

    /**
     * Register a new state
     */
    fun init(state: State<*>) {
        this.stateMap[state.name] = state
    }

    /**
     * Reset state to its default value if it is present
     */
    fun invalidate(stateName: String) {
        stateMap[stateName]?.invalidate()
    }

    /**
     * Update logical state if it is changed. If argument is Meta or MetaMorph, then redirect to {@link updateLogicalMetaState}
     *
     * @param stateName
     * @param stateValue
     */
    fun update(stateName: String, stateValue: Any?) {
        val state = stateMap.getOrPut(stateName) {
            logger.warn("State with name $stateName is not registered. Creating new logical state")
            when (stateValue) {
                is Meta -> MetaState(stateName).also { init(it) }
                is MetaMorph -> MorphState(stateName, (stateValue as MetaMorph)::class)
                else -> ValueState(stateName).also { init(it) }
            }
        }

        state.update(stateValue)
        logger.info("State {} changed to {}", stateName, stateValue)
    }

    override fun optValue(path: String): Optional<Value> {
        return (get(path) as? ValueState)?.value.optional
    }
}
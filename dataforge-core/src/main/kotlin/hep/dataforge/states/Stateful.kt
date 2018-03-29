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

import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames


/**
 * An object that could have a set of readonly or read/write states
 */
interface Stateful : Provider {

    val states: Collection<State<*>>

    @Provides(STATE_TARGET)
    fun optState(stateName: String): State<*>? {
        return states.find { it.name == stateName }
    }

    @get:ProvidesNames(STATE_TARGET)
    val stateNames: Collection<String>
        get() = states.map { it.name }

//    /**
//     * A list of all available states
//     *
//     * @return
//     */
//    val stateDefs: List<StateDef>
//        get() = listAnnotations(this.javaClass, StateDef::class.java, true)
//
//
//    /**
//     * A list of all available metastates
//     *
//     * @return
//     */
//    val metaStateDefs: List<MetaStateDef>
//        get() = listAnnotations(this.javaClass, MetaStateDef::class.java, true)
//
//    /**
//     * Get the state with given name. Null if such state not found or
//     * undefined. This operation is synchronous so use it with care. In general,
//     * it is recommended to use asynchronous state change listeners instead of
//     * this method.
//     *
//     * @param name
//     * @return
//     */
//    fun getState(name: String): Value?
//
//    fun getMetaState(name: String): Meta?
//
//    @Provides(STATE_TARGET)
//    open fun optState(stateName: String): Optional<Value> {
//        if (!hasState(stateName)) {
//            return Optional.empty()
//        } else {
//            val state = getState(stateName)
//            return if (state == null || state.isNull) {
//                Optional.empty()
//            } else {
//                Optional.of(state)
//            }
//        }
//    }
//
//    @Provides(METASTATE_TARGET)
//    open fun optMetaState(stateName: String): Optional<Meta> {
//        if (!hasMetaState(stateName)) {
//            return Optional.empty()
//        } else {
//            val state = getMetaState(stateName)
//            return if (state == null || state.isEmpty) {
//                Optional.empty()
//            } else {
//                Optional.of(state)
//            }
//        }
//    }
//
//    @ProvidesNames(STATE_TARGET)
//    fun listStates(): Stream<String> {
//        return stateDefs.stream().map { it -> it.value.name }
//    }
//
//    @ProvidesNames(METASTATE_TARGET)
//    fun listMetaStates(): Stream<String> {
//        return metaStateDefs.stream().map { it -> it.value.name }
//    }
//
//    fun optBooleanState(name: String): Optional<Boolean> {
//        return optState(name).map{ it.booleanValue() }
//    }
//
//    /**
//     * Request asynchronous state change for state with given name and return the state value after change
//     *
//     * @param name
//     * @param value
//     */
//    fun setState(name: String, value: Any)
//
//    fun setMetaState(name: String, value: Meta)
//
//    /**
//     * Find if current device has defined state with given name
//     *
//     * @param stateName
//     * @return
//     */
//    fun hasState(stateName: String): Boolean {
//        return stateDefs.stream().anyMatch { stateDef -> stateDef.value.name == stateName }
//    }
//
//    /**
//     * Find a state definition for given name. Null if not found.
//     *
//     * @param name
//     * @return
//     */
//    fun optStateDef(name: String): Optional<StateDef> {
//        return stateDefs.stream().filter { stateDef -> stateDef.value.name == name }.findFirst()
//    }
//
//    /**
//     * Find if current device has defined metastate with given name
//     *
//     * @param stateName
//     * @return
//     */
//    fun hasMetaState(stateName: String): Boolean {
//        return metaStateDefs.stream().anyMatch { stateDef -> stateDef.value.name == stateName }
//    }
//
//    /**
//     * Find a state definition for given name. Null if not found.
//     *
//     * @param name
//     * @return
//     */
//    fun optMetaStateDef(name: String): Optional<MetaStateDef> {
//        return metaStateDefs.stream().filter { stateDef -> stateDef.value.name == name }.findFirst()
//    }
//
//    /**
//     * Get the descriptor for a given state
//     * @param stateName
//     * @return
//     */
//    fun getStateDescriptor(stateName: String): Optional<ValueDef> {
//        return optStateDef(stateName).map{ it.value }
//    }
//
//    /**
//     * Get descriptor for a meta state. Could be overriden for a custom descriptor
//     * @param stateName
//     * @return
//     */
//    fun getMetaStateDescriptor(stateName: String): Optional<NodeDef> {
//        return optMetaStateDef(stateName).map{ it.value }
//    }

    companion object {

        const val STATE_TARGET = "state"
    }
}

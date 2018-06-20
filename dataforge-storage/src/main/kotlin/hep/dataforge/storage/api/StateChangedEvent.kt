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

import hep.dataforge.events.Event
import hep.dataforge.events.EventBuilder
import hep.dataforge.kodex.stringValue
import hep.dataforge.kodex.value
import hep.dataforge.meta.Meta
import hep.dataforge.values.Value

/**
 * The event describing change of some state in the state loader
 *
 * @author Alexander Nozik
 */
class StateChangedEvent(meta: Meta) : Event(meta) {

    /**
     * The name or path of the changed state
     *
     * @return
     */
    val stateName: String by meta.stringValue(STATE_NAME_KEY)

    /**
     * The value before change
     *
     * @return
     */
    val oldState: Value by meta.value(OLD_STATE_KEY)

    /**
     * The value after change
     *
     * @return
     */
    val newState: Value by meta.value(NEW_STATE_KEY)

    override fun toString(): String {
        return String.format("(%s) [%s] : changed state '%s' from %s to %s", time().toString(), sourceTag(), stateName, oldState.string, newState.string)
    }

    companion object {

        const val STATE_NAME_KEY = "stateName"
        const val OLD_STATE_KEY = "oldState"
        const val NEW_STATE_KEY = "newState"

        //TODO add source declaration here
        fun build(stateName: String, oldState: Value, newState: Value): StateChangedEvent {
            return StateChangedEvent(builder(stateName, oldState, newState).buildEventMeta())
        }

        fun builder(stateName: String, oldState: Value, newState: Value): EventBuilder<*> {
            return EventBuilder.make("storage.stateChange")
                    .setMetaValue(STATE_NAME_KEY, stateName)
                    .setMetaValue(OLD_STATE_KEY, oldState)
                    .setMetaValue(NEW_STATE_KEY, newState)
        }
    }
}

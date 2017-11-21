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

import hep.dataforge.events.Event;
import hep.dataforge.events.EventBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * The event describing change of some state in the state loader
 *
 * @author Alexander Nozik
 */
public class StateChangedEvent extends Event {

    public static final String STATE_NAME_KEY = "stateName";
    public static final String OLD_STATE_KEY = "oldState";
    public static final String NEW_STATE_KEY = "newState";

    //TODO add source declaration here
    public static StateChangedEvent build(String stateName, Value oldState, Value newState) {
        return new StateChangedEvent(builder(stateName, oldState, newState).buildEventMeta());
    }

    public static EventBuilder builder(String stateName, Value oldState, Value newState) {
        return EventBuilder.make("storage.stateChange")
                .setMetaValue(STATE_NAME_KEY, stateName)
                .setMetaValue(OLD_STATE_KEY, oldState)
                .setMetaValue(NEW_STATE_KEY, newState);
    }

    public StateChangedEvent(Meta meta) {
        super(meta);
    }

    /**
     * The name or path of the changed state
     *
     * @return
     */
    public String stateName() {
        return getMeta().getString(STATE_NAME_KEY);
    }

    /**
     * The value before change
     *
     * @return
     */
    public Value oldState() {
        return getMeta().getValue(OLD_STATE_KEY);
    }

    /**
     * The value after change
     *
     * @return
     */
    public Value newState() {
        return getMeta().getValue(NEW_STATE_KEY);
    }

    @Override
    public String toString() {
        return String.format("(%s) [%s] : changed state '%s' from %s to %s", time().toString(), sourceTag(), stateName(), oldState().stringValue(), newState().stringValue());
    }
}

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
import hep.dataforge.values.Value;

/**
 * The event describing change of some state in the state loader
 *
 * @author Alexander Nozik
 */
public interface StateChangedEvent extends Event {

    /**
     * The loader that generated this event
     *
     * @return
     */
    StateLoader loader();
    
    /**
     * The name or path of the changed state
     * @return 
     */
    String stateName();

    /**
     * The value before change
     *
     * @return
     */
    Value oldState();

    /**
     * The value after change
     * @return 
     */
    Value newState();
    
}

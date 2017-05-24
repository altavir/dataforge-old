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
import hep.dataforge.events.EventHandler;
import hep.dataforge.io.envelopes.Envelope;

/**
 * The loader for events. By default it does not provide any pull operations and
 * could ignore events partially or store only limited number of entries
 *
 * @author darksnake
 */

public interface EventLoader extends Loader, Iterable<Event>, EventHandler {

    String EVENT_LOADER_TYPE = "event";

    @Override
    boolean pushEvent(Event event);

    @Override
    default String getType() {
        return EVENT_LOADER_TYPE;
    }


    @Override
    default Envelope respond(Envelope message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

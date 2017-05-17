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
package hep.dataforge.events;

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Metoid;

import java.time.Instant;
import java.util.Optional;

import static hep.dataforge.events.Event.*;

/**
 * Генеральный класс для событий всех возможных типов
 *
 * @author Alexander Nozik
 */
@ValueDef(name = EVENT_TYPE_KEY, required = true, info = "The type of the ivent in standard dot notation")
@ValueDef(name = EVENT_PRIORITY_KEY, type = "NUMBER", def = "0", info = "Priority of the event. 0 is default priority")
@ValueDef(name = EVENT_SOURCE_KEY, def = "", info = "The source or the tag of the event. By default is empty")
@ValueDef(name = EVENT_TIME_KEY, required = true, info = "Time of the event")
public interface Event extends Metoid {

    String EVENT_PRIORITY_KEY = "priority";
    String EVENT_TYPE_KEY = "type";
    String EVENT_SOURCE_KEY = "sourceTag";
    String EVENT_TIME_KEY = "time";

    //PENDING allow weak references to objects in events?
    default int priority() {
        return meta().getInt(EVENT_PRIORITY_KEY, 0);
    }

    default String type() {
        return meta().getString(EVENT_TYPE_KEY);
    }

    default String sourceTag() {
        return meta().getString(EVENT_SOURCE_KEY, "");
    }

    default Instant time() {
        return meta().getValue(EVENT_TIME_KEY).timeValue();
    }

    /**
     * Get an object reference. By default, all objects inside events are stored
     * as weak references, so the could be GC-ed before events is evaluated.
     *
     * @param tag
     * @return
     */
    Optional getReference(String tag);

    /**
     * get event string representation (header) to write in logs
     *
     * @return
     */
    @Override
    String toString();
}

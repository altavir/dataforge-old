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
import static hep.dataforge.events.Event.*;
import hep.dataforge.meta.Annotated;
import java.time.Instant;

/**
 * Генеральный класс для событий всех возможных типов
 *
 * @author Alexander Nozik
 */
@ValueDef(name = EVENT_TYPE_KEY, required = true, info = "The type of the ivent in standard dot notation")
@ValueDef(name = EVENT_PRIORITY_KEY, type = "NUMBER",def = "0", info = "Priority of the event. 0 is default priority")
@ValueDef(name = EVENT_SOURCE_KEY, def = "", info = "The source or the tag of the event. By default is empty")
@ValueDef(name = EVENT_TIME_KEY, required = true, info = "Time of the event")
public interface Event extends Annotated {

    public static final String EVENT_PRIORITY_KEY = "priority";
    public static final String EVENT_TYPE_KEY = "type";
    public static final String EVENT_SOURCE_KEY = "source";
    public static final String EVENT_TIME_KEY = "source";    

    default int priority() {
        return meta().getInt(EVENT_PRIORITY_KEY, 0);
    }

    default String type() {
        return meta().getString(EVENT_TYPE_KEY);
    }

    default String source() {
        return meta().getString(EVENT_SOURCE_KEY,"");
    }

    default Instant time(){
        return meta().getValue(EVENT_TIME_KEY).timeValue();
    }

    /**
     * get event string representation (header) to write in logs
     *
     * @return
     */
    @Override
    String toString();
}

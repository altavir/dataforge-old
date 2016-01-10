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
package hep.dataforge.control.tasks;

import hep.dataforge.events.BasicEvent;
import java.time.Instant;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class TaskCompleteEvent<T> extends BasicEvent {

    private T value;
    private Class valueType;

    public TaskCompleteEvent(String tag, T value) {
        this(tag, Instant.now(), value);
    }

    public TaskCompleteEvent(String source, Instant time, T value) {
        super("control.task.complete", source, 0, time, null);
        this.value = value;
        valueType = value.getClass();
    }

    public Class getValueType() {
        return valueType;
    }

    public T value() {
        return value;
    }

}

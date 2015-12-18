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

import hep.dataforge.events.Event;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class TaskCompleteEvent<T> implements Event {

    private Instant time;
    private T value;
    private Class valueType;
    private String tag;

    public TaskCompleteEvent(String tag, T value) {
        this(tag, Instant.now(), value);
    }

    public TaskCompleteEvent(String tag, Instant time, T value) {
        this.time = time;
        this.value = value;
        valueType = value.getClass();
        this.tag = tag;
    }

    public Class getValueType() {
        return valueType;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String source() {
        return tag;
    }

    public T value() {
        return value;
    }

    @Override
    public String type() {
        return getClass().getSimpleName();
    }

    @Override
    public Instant time() {
        return time;
    }

}

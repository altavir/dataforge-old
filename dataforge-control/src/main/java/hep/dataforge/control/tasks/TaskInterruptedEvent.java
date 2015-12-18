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
 */
public class TaskInterruptedEvent implements Event {

    private final String tag;
    private final Instant time;
    private final Throwable exception;

    public TaskInterruptedEvent(String tag, Instant time, Throwable exception) {
        this.tag = tag;
        this.time = time;
        this.exception = exception;
    }
    
    public TaskInterruptedEvent(String tag, Instant time) {
        this.tag = tag;
        this.time = time;
        this.exception = null;
    }

    public TaskInterruptedEvent(String tag) {
        this.tag = tag;
        time = Instant.now();
        this.exception = null;        
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public String source() {
        return tag;
    }

    @Override
    public Instant time() {
        return time;
    }

    @Override
    public String type() {
        return getClass().getSimpleName();
    }

    public Throwable getException() {
        return exception;
    }
    
    

}

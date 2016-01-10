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
 */
public class TaskInterruptedEvent extends BasicEvent {

    private final Throwable exception;

    public TaskInterruptedEvent(String source, Instant time, Throwable exception) {
        super("control.task.interrupted", source, 0, time, null);        
        this.exception = exception;
    }
    
    public TaskInterruptedEvent(String source, Instant time) {
        this(source, time, null);
    }

    public TaskInterruptedEvent(String source) {
        this(source, null, null);       
    }

    public Throwable getException() {
        return exception;
    }
    
    

}

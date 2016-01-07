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

import java.time.Instant;

/**
 * Генеральный класс для событий всех возможных типов
 * @author Alexander Nozik
 */
public interface Event {
    int priority();
    String type();
    String source();
    Instant time();
    
    /**
     * get event string representation (header) to write in logs
     * @return 
     */
    @Override
    String toString();
    
    //TODO make annotation or ValueProvider representation of events
}

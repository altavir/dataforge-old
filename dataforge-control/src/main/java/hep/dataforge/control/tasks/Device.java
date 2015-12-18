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

import hep.dataforge.content.Content;
import hep.dataforge.events.EventHandler;
import hep.dataforge.values.Value;

/**
 *
 * @author Alexander Nozik
 */
public interface Device extends Content, EventHandler{
    <V> ControlTask<V> getState(String... parameters);
    <V> ControlTask<V> setState(Value state, String... parameters);
    
    boolean addListner(String handlerTag, EventHandler handler);
    boolean removeLisner(String handlerTag);
}

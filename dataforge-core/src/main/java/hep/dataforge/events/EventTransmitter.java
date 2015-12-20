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

/**
 * An interface marking an object that can dispatch messages (send it to named
 * responder)
 *
 * @author Alexander Nozik
 */
public interface EventTransmitter<T extends Event> {

    /**
     * Send message and return true if message is successfully sent
     * @param address
     * @param event
     * @return 
     */
    boolean send(String address, T event);
}
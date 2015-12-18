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
package hep.dataforge.control.devices;

import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * A listener that listens to device state change initialization and shut down
 * @author Alexander Nozik
 */
public interface DeviceListener {

    /**
     * The device is initialized. No measurement or control procedure performed here
     * @param device 
     */
    void notifyDeviceInitialized(Device device);

    /**
     * The device is shut down. No 
     * @param device 
     */
    void notifyDeviceShutdown(Device device);

    /**
     * Notify that state of device is changed. either oldState or newState could
     * be empty.
     *
     * @param device
     * @param name the name of the state
     * @param oldState
     * @param newState
     */
    void notifyDeviceStateChanged(Device device, String name, Value oldState, Value newState);

//    /**
//     * Ask listener to send or display given message
//     * @param device
//     * @param priority
//     * @param message 
//     */
//    void sendMessage(Device device, int priority, Meta message);
//    
//    /**
//     * Notify listener that device received and accepted message.
//     * @param device
//     * @param message 
//     */
//    void notifyMessageRecieved(Device device, Meta message);
    
}

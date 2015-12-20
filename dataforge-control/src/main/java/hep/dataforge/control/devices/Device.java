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

import hep.dataforge.control.connections.Connection;
import hep.dataforge.content.Named;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Responder;
import hep.dataforge.meta.Annotated;
import hep.dataforge.values.Value;
import hep.dataforge.context.Encapsulated;

/**
 *
 * @author Alexander Nozik
 */
public interface Device extends Annotated, Encapsulated, Named, Responder {

    /**
     * Get the device state with given name. Null if such state not found or
     * undefined;
     *
     * @param name
     * @return
     */
    Value getState(String name);

    /**
     * Initialize device and check connection but do not start it. Init method
     * could be called only once per MeasurementDevice object. On second call it
     * throws exception or does nothing.
     *
     * @throws ControlException
     */
    void init() throws ControlException;

    /**
     * Release all resources locked during init. No further work with device is
     * possible after shutdown. The init method called after shutdown can cause
     * exceptions or incorrect work.
     *
     * @throws ControlException
     */
    void shutdown() throws ControlException;

    /**
     * Set device state listener for this device. Setting null removes current
     * device state listener.
     *
     * @param listener
     */
    void addDeviceListener(DeviceListener listener);
    
//    /**
//     * Add connection to device
//     * @param connection 
//     */
//    void connect(Connection connection);
    
    /**
     * Get a named and type checked connection for this device.
     * @param <T>
     * @param name
     * @param type
     * @return 
     */
    <T extends Connection> T getConnection(String name, Class<T> type);
    
    /**
     * Get a named connection for this device.
     * @param name
     * @return 
     */
    Connection getConnection(String name);

}

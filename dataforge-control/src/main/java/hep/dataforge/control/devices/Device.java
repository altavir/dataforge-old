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
import hep.dataforge.values.Value;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;

/**
 * The Device is general abstract representation of any physical or virtual
 * apparatus that can interface with data acquisition and control system.
 * <p>
 * The device has following important features:
 * </p>
 * <ul>
 * <li>
 * <strong>States:</strong> each device has a number of states that could be
 * accessed by {@code getState} method. States could be either stored as some
 * internal variables or calculated on demand. States calculation is
 * synchronous!
 * </li>
 * <li>
 * <strong>Listeners:</strong> some external class which listens device state
 * changes and events. By default listeners are represented by weak references
 * so they could be finalized any time if not used.
 * </li>
 * <li>
 * <strong>Connections:</strong> any external device connectors which are used
 * by device. The difference between listener and connection is that device is
 * obligated to notify all registered listeners about all changes, but
 * connection is used by device at its own discretion. Also usually only one
 * connection is used for each single purpose.
 * </li>
 * <li>
 * <strong>Commands:</strong> commands could be issued to device with or without
 * additional meta. Commands are accepted and executed asynchronously.
 * </li>
 * </ul>
 *
 * @author Alexander Nozik
 */
public interface Device extends Configurable, Encapsulated, Named, Responder {

    //TODO add device states annotations
    /**
     * Device type
     * @return 
     */
    String type();
    
    /**
     * Get the device state with given name. Null if such state not found or
     * undefined. This operation is synchronous so use it with care. In general,
     * it is recommended to use asynchronous state change listeners instead of
     * this method.
     *
     * @param name
     * @return
     */
    Value getState(String name);

    /**
     * Initialize device and check if it is working but do not start any
     * measurements or issue commands. Init method could be called only once per
     * MeasurementDevice object. On second call it throws exception or does
     * nothing.
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

    /**
     * Add a device listener with strong reference
     *
     * @param listener
     */
    void addStrongDeviceListener(DeviceListener listener);
    
    /**
     * remove a listener
     * @param listenrer 
     */
    void removeDeviceListener(DeviceListener listenrer);

    /**
     * Get a named connection for this device.
     *
     * @param name
     * @return
     */
    Connection getConnection(String name);

    /**
     * Send command to the device. This method does not ensure that command is
     * accepted. Command is not necessarily is executed immediately, it could be
     * posted to the command queue according to its priority.
     *
     * @param comand
     */
    void command(String command, Meta commandMeta) throws ControlException;

    /**
     * Send command without additional meta or using default meta for this
     * command
     *
     * @param command
     */
    default void command(String command) throws ControlException {
        command(command, null);
    }

    /**
     * Send command using 'command' value from meta as a name. If command name
     * is not provided, than empty name is used.
     *
     * @param commandMeta
     */
    default void command(Meta commandMeta) throws ControlException {
        command(commandMeta.getString("command", ""), commandMeta);
    }

}

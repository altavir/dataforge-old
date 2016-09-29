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

import hep.dataforge.context.Encapsulated;
import hep.dataforge.control.connections.Connection;
import hep.dataforge.control.devices.annotations.RoleDef;
import hep.dataforge.control.devices.annotations.StateDef;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Responder;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.values.Value;

import java.util.List;

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
 * <li>
 * <strong>Connections:</strong> any external device connectors which are used
 * by device. The difference between listener and connection is that device is
 * obligated to notify all registered listeners about all changes, but
 * connection is used by device at its own discretion. Also usually only one
 * connection is used for each single purpose.
 * </li>
 * </ul>
 *
 * @author Alexander Nozik
 */
public interface Device extends Configurable, Annotated, Encapsulated, Named, Responder {

    /**
     * Device type
     *
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

//    void setState(String stateName, Object stateValue);
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
     *
     * @param listenrer
     */
    void removeDeviceListener(DeviceListener listenrer);

    /**
     * Register connection for this device
     *
     * @param connection
     * @param roles a set of roles for this connection
     */
    void connect(Connection<Device> connection, String... roles);

    /**
     * Invoke a simple command (set state) for this device
     *
     * @param commandName
     * @param argument
     */
    void command(String commandName, Value argument) throws ControlException;

    /**
     * A command with complex configuration
     *
     * @param commandName
     * @param commandConfiguration
     */
    void command(String commandName, Meta commandConfiguration) throws ControlException;

    /**
     * A list of all available states
     *
     * @return
     */
    default List<StateDef> stateDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), StateDef.class, true);
    }

    /**
     * A list of all available roles
     *
     * @return
     */
    default List<RoleDef> roleDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), RoleDef.class, true);
    }

    /**
     * Find a state definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default StateDef getStateDef(String name) {
        return stateDefs().stream().filter((stateDef) -> stateDef.name().equals(name)).findFirst().orElse(null);
    }

    /**
     * A quick way to find if device accepts connection with given role
     *
     * @param name
     * @return
     */
    default boolean hasRole(String name) {
        return roleDefs().stream().filter((roleDef) -> roleDef.name().equals(name)).findAny().isPresent();
    }

    /**
     * Find a role definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default RoleDef getRoleDef(String name) {
        return roleDefs().stream().filter((roleDef) -> roleDef.name().equals(name)).findFirst().orElse(null);
    }

}

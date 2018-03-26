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

import hep.dataforge.Named;
import hep.dataforge.connections.AutoConnectible;
import hep.dataforge.connections.RoleDef;
import hep.dataforge.context.ContextAware;
import hep.dataforge.control.ControlUtils;
import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.states.Stateful;
import org.slf4j.Logger;

import static hep.dataforge.connections.Connection.EVENT_HANDLER_ROLE;
import static hep.dataforge.connections.Connection.LOGGER_ROLE;
import static hep.dataforge.control.connections.Roles.DEVICE_LISTENER_ROLE;
import static hep.dataforge.control.connections.Roles.VIEW_ROLE;


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
@RoleDef(name = DEVICE_LISTENER_ROLE, objectType = DeviceListener.class, info = "A device listener")
@RoleDef(name = LOGGER_ROLE, objectType = Logger.class, unique = true, info = "The logger for this device")
@RoleDef(name = EVENT_HANDLER_ROLE, objectType = EventHandler.class, info = "The listener for device events")
@RoleDef(name = VIEW_ROLE)
public interface Device extends AutoConnectible, Metoid, ContextAware, Named, Responder, Stateful {

    String INITIALIZED_STATE = "init";

    /**
     * Device type
     *
     * @return
     */
    String getType();

    /**
     * True if device is initialized and not shut down
     * @return
     */
    default boolean isInitialized(){
        return optBooleanState(INITIALIZED_STATE).orElse(false);
    }

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

    @Override
    default Envelope respond(Envelope message) {
        return ControlUtils.getDefaultDeviceResponse(this, message);
    }

    @Override
    default Logger getLogger() {
        return optConnection(LOGGER_ROLE, Logger.class).orElse(getContext().getLogger());
    }
}

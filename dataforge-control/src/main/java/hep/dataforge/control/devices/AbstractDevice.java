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

import hep.dataforge.content.AnonimousNotAlowed;
import hep.dataforge.context.Context;
import hep.dataforge.control.connections.Connection;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.BaseConfigurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ReferenceRegistry;
import hep.dataforge.values.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * State have two components: physical and logical. If logical state does not
 * coincide with physical, it should be invalidated and automatically updated on
 * next request.
 * </p>
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public abstract class AbstractDevice extends BaseConfigurable implements Device {

    private final Context context;
    private final String name;
    private final ReferenceRegistry<DeviceListener> listeners = new ReferenceRegistry<>();
    private final Map<String, Connection> connections = new HashMap<>();
    private final Map<String, Value> states = new ConcurrentHashMap<>();
    private Logger logger;

    public AbstractDevice(String name, Context context, Meta meta) {
        super(meta, context);
        this.name = name;
        this.context = context;
    }

    protected Logger setupLogger() {
        //TODO move logger construction to context IoManager
        return LoggerFactory.getLogger(getClass());
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void init() throws ControlException {
        logger = setupLogger();
        logger.info("Initializing device '{}'...", getName());
        listeners.forEach(it -> it.notifyDeviceInitialized(this));
    }

    @Override
    public void shutdown() throws ControlException {
        logger.info("Shutting down device '{}'...", getName());
        listeners.forEach(it -> it.notifyDeviceShutdown(this));
        //TODO close connections and close listeners
        logger = null;
    }

    @Override
    public void addDeviceListener(DeviceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addStrongDeviceListener(DeviceListener listener) {
        listeners.add(listener, true);
    }

    @Override
    public void removeDeviceListener(DeviceListener listenrer) {
        listeners.remove(listenrer);
    }

    @Override
    public Connection getConnection(String name) {
        return connections.get(name);
    }

    @Override
    public void command(String command, Meta commandMeta) throws ControlException {
        if (logger != null) {
            logger.debug("Recieved command {}", command);
        }
        if (checkCommand(command, commandMeta)) {
            listeners.forEach(it -> it.notifyDeviceCommandAccepted(this, command, commandMeta));
            evalCommand(command, commandMeta);
        } else {
            logger.error("Command {} rejected", command);
        }
    }

    /**
     * Do evaluate command
     *
     * @param command
     * @param commandMeta
     * @throws ControlException
     */
    protected abstract void evalCommand(String command, Meta commandMeta) throws ControlException;

    /**
     * Check if command could be evaluated by this device
     *
     * @param command
     * @param commandMeta
     * @return
     */
    protected boolean checkCommand(String command, Meta commandMeta) {
        return true;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Envelope respond(Envelope message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Update logical state and notify listeners.
     *
     * @param stateName
     * @param stateValue
     */
    protected final void notifyStateChanged(String stateName, Value stateValue) {
        this.states.put(name, stateValue);
        listeners.forEach((DeviceListener it) -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, stateValue));
    }

    /**
     * Post a value to the state and notify state changed if needed. Does not
     * change physical configuration
     *
     * @param stateName
     * @param stateValue
     */
    protected void updateState(String stateName, Object stateValue) {
        Value oldState = this.states.get(stateName);
        Value newState = Value.of(stateValue);
        //Notify only if state realy changed
        if (!newState.equals(oldState)) {
            notifyStateChanged(stateName, newState);
        }
    }

    protected final void notifyError(String message, Throwable error) {
        listeners.forEach((DeviceListener it) -> it.evaluateDeviceException(AbstractDevice.this, message, error));
    }

    /**
     * Invalidate a state and force recalculate on next request
     *
     * @param stateName
     */
    protected final void invalidateState(String stateName) {
        this.states.remove(name);
//        listeners.forEach((DeviceListener it) -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, null));
    }

    /**
     * Set state of the device changing its physical configuration if needed
     *
     * @param stateName
     * @param stateValue
     */
    public void setState(String stateName, Object stateValue) {
        try {
            Value val = Value.of(stateValue);
            if (applyState(stateName, val)) {
                notifyStateChanged(stateName, val);
            } else {
                getLogger().warn("State {} not changed", stateName);
            }
        } catch (ControlException ex) {
            notifyError("Can't change state " + stateName, ex);
        }
    }

    /**
     * Apply state to device
     *
     * @param stateName
     * @param stateValue
     * @return
     */
    protected boolean applyState(String stateName, Value stateValue) throws ControlException {
        throw new ControlException("State " + stateName + " is not defined or read only");
    }

    protected abstract Object calculateState(String stateName) throws ControlException;

    @Override
    public Value getState(String stateName) {
        return this.states.computeIfAbsent(stateName, (String t) -> {
            try {
                return Value.of(calculateState(stateName));
            } catch (ControlException ex) {
                notifyError("Can't calculate stat " + stateName, ex);
                return null;
            }
        });
    }

    /**
     * Attach connection
     *
     * @param name
     * @param connection
     * @return
     * @throws Exception
     */
    public synchronized void connect(String name, Connection connection) throws Exception {
        this.connections.put(name, connection);
    }

    @Override
    protected void applyConfig(Meta config) {
        if (logger != null) {
            logger.debug("Applying configuration change");
        }
        listeners.forEach((DeviceListener it) -> it.notifyDeviceConfigChanged(AbstractDevice.this));
    }

}

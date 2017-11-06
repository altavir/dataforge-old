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

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.control.Connection;
import hep.dataforge.control.ConnectionHelper;
import hep.dataforge.events.Event;
import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.utils.MetaHolder;
import hep.dataforge.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static hep.dataforge.control.connections.Roles.DEVICE_LISTENER_ROLE;

/**
 * <p>
 * State have two components: physical and logical. If logical state does not
 * coincide with physical, it should be invalidated and automatically updated on
 * next request.
 * </p>
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
public abstract class AbstractDevice extends MetaHolder implements Device {
    //TODO set up logger as connection

    private final Map<String, Value> states = new HashMap<>();
    private Context context;
    private ConnectionHelper connectionHelper;

    private Logger getDefaultLogger() {
        String loggerName = meta().getString("logger", () -> "device::" + getName());
        return LoggerFactory.getLogger(loggerName);
    }

    public Logger getLogger() {
        return getDefaultLogger();
        //return connections(LOGGER_ROLE, Logger.class).findFirst().orElseGet(this::getDefaultLogger);
    }

    public ConnectionHelper getConnectionHelper() {
        if (connectionHelper == null) {
            connectionHelper = new ConnectionHelper(this, this.getLogger());
        }
        return connectionHelper;
    }


    @Override
    public void init() throws ControlException {
        getLogger().info("Initializing device '{}'...", getName());
        updateState(INITIALIZED_STATE, true);
    }

    @Override
    public void shutdown() throws ControlException {
        getLogger().info("Shutting down device '{}'...", getName());
        forEachConnection(Connection.class, c -> {
            try {
                c.close();
            } catch (Exception e) {
                getLogger().error("Failed to close connection", e);
            }
        });
        updateState(INITIALIZED_STATE, false);
//        forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class, it -> it.notifyDeviceShutdown(this));
    }


    @Override
    public Context getContext() {
        if (context == null) {
            getLogger().warn("Context for device not defined. Using GLOBAL context.");
            return Global.instance();
        } else {
            return this.context;
        }
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return meta().getString("name", getType());
    }

    /**
     * Update logical state and notify listeners.
     *
     * @param stateName
     * @param stateValue
     */
    private void notifyStateChanged(String stateName, Value stateValue) {
        this.states.put(stateName, stateValue);
        if (stateValue.isNull()) {
            getLogger().info("State {} is reset", stateName);
        } else {
            getLogger().info("State {} changed to {}", stateName, stateValue);
        }
        forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                it -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, stateValue));
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
        //Notify only if state really changed
        if (!newState.equals(oldState)) {
            notifyStateChanged(stateName, newState);
        }
    }

    protected final void notifyError(String message, Throwable error) {
        getLogger().error(message, error);
        forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                it -> it.evaluateDeviceException(AbstractDevice.this, message, error));
    }

    protected final void dispatchEvent(Event event) {
        forEachConnection(EventHandler.class, it -> it.pushEvent(event));
    }

    /**
     * Invalidate a state and force recalculate on next request
     *
     * @param stateName
     */
    protected final void invalidateState(String stateName) {
        this.states.remove(stateName);
    }

    /**
     * Force invalidate and immediately recalculate state
     *
     * @param stateName
     */
    protected final void recalculateState(String stateName) {
        try {
            updateState(stateName, computeState(stateName));
        } catch (ControlException ex) {
            notifyError("Can't calculate state " + stateName, ex);
        }
    }

    protected Object computeState(String stateName) throws ControlException {
        return optStateDef(stateName).map(def -> def.value().def())
                .orElseThrow(() -> new ControlException("Can't calculate state " + stateName));
    }

    protected void requestStateChange(String stateName, Value value) throws ControlException {
        updateState(stateName, value);
    }

    @Override
    public CompletableFuture<Value> setState(String stateName, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                requestStateChange(stateName, Value.of(value));
            } catch (ControlException e) {
                getLogger().error("Failed to set state {} to {} with exception: {}", stateName, value, e.toString());
                return Value.NULL;
            }
            return getState(stateName);
        });
    }

    @Override
    public Value getState(String stateName) {
        return this.states.computeIfAbsent(stateName, (String t) -> {
            try {
                Value newState = Value.of(computeState(stateName));
                notifyStateChanged(stateName, newState);
                return newState;
            } catch (ControlException ex) {
                notifyError("Can't calculate state " + stateName, ex);
                return Value.NULL;
            }
        });
    }

    @Override
    public Optional<Value> optState(String stateName) {
        if (states.containsKey(stateName)) {
            return Optional.of(states.get(stateName));
        } else {
            return Device.super.optState(stateName);
        }
    }

    @Override
    public String getType() {
        return meta().getString("type", "unknown");
    }
}

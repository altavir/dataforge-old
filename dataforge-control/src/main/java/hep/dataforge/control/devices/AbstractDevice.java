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
import hep.dataforge.control.connections.Connection;
import hep.dataforge.control.devices.annotations.RoleDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.BaseConfigurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Named;
import hep.dataforge.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
@AnonimousNotAlowed
@RoleDef(name = DEVICE_LISTENER_ROLE, objectType = DeviceListener.class, info = "A device listener")
public abstract class AbstractDevice extends BaseConfigurable implements Device {
    //TODO set up logger as connection

    //private final ReferenceRegistry<DeviceListener> listeners = new ReferenceRegistry<>();
    private final Map<Connection<? extends Device>, List<String>> connections = new ConcurrentHashMap<>();
    private final Map<String, Value> states = new ConcurrentHashMap<>();
    private Context context;
    private Logger logger;

    private Logger setupLogger() {
        String loggerName = meta().getString("logger", () -> "device::" + getName());

        //TODO move logger construction to context IoManager
        return LoggerFactory.getLogger(loggerName);
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = setupLogger();
        }
        return logger;
    }

    @Override
    public void init() throws ControlException {
        getLogger().info("Initializing device '{}'...", getName());
        updateState(INITIALIZED_STATE, true);
//        forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class, it -> it.notifyDeviceInitialized(this));
    }

    @Override
    public void shutdown() throws ControlException {
        getLogger().info("Shutting down device '{}'...", getName());
        connections().forEach(it -> {
            try {
                it.getKey().close();
            } catch (Exception e) {
                getLogger().error("Failed to close connection {} with roles", it.getKey(), it.getValue());
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
        setValueContext(context);
    }

    @Override
    public String getName() {
        return meta().getString("name", type());
    }

//    public void setName(String name) {
//        this.getConfig().setValue("name", name);
//    }

    /**
     * Update logical state and notify listeners.
     *
     * @param stateName
     * @param stateValue
     */
    protected final void notifyStateChanged(String stateName, Value stateValue) {
        this.states.put(stateName, stateValue);
        getLogger().info("State {} changed to {}", stateName, stateValue);
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

    /**
     * Invalidate a state and force recalculate on next request
     *
     * @param stateName
     */
    protected final void invalidateState(String stateName) {
        this.states.put(stateName, Value.NULL);
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

    protected abstract Object computeState(String stateName) throws ControlException;

    protected abstract void requestStateChange(String stateName, Value value) throws ControlException;

    @Override
    public Future<Value> setState(String stateName, Object value) {
        try {
            requestStateChange(stateName, Value.of(value));
        } catch (ControlException e) {
            getLogger().error("Failed to set state {} to {} with exception: {}", stateName, value, e.toString());
            return CompletableFuture.completedFuture(Value.NULL);
        }
        return CompletableFuture.supplyAsync(() -> getState(stateName));
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

    /**
     * Attach connection
     *
     * @param connection
     * @param roles
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void connect(Connection connection, String... roles) {
        getLogger().info("Attaching connection {} with roles {}", connection.toString(), String.join(", ", roles));
        //Checking if connection could serve given roles
        for (String role : roles) {
            if (!acceptsRole(role)) {
                getLogger().warn("The device {} does not support role {}", getName(), role);
            } else {
                roleDefs().stream().filter((roleDef) -> roleDef.name().equals(role)).forEach(rd -> {
                    if (!rd.objectType().isInstance(connection)) {
                        getLogger().error("Connection does not meet type requirement for role {}. Must be {}.",
                                role, rd.objectType().getName());
                    }
                });
            }
        }
        this.connections.put(connection, Arrays.asList(roles));
        try {
            getLogger().debug("Opening connection {}", connection.toString());
            connection.open(this);
        } catch (Exception ex) {
            this.notifyError("Can not open connection", ex);
        }
    }

    public synchronized void disconnect(Connection<Device> connection) {
        if (connections.containsKey(connection)) {
            String conName = Named.nameOf(connection);
            try {
                getLogger().debug("Closing connection {}", conName);
                connection.close();
            } catch (Exception ex) {
                this.notifyError("Can not close connection", ex);
            }
            getLogger().info("Detaching connection {}", conName);
            this.connections.remove(connection);
        }
    }

    /**
     * For each connection of given class and role. Role may be empty, but type
     * is mandatory
     *
     * @param <T>
     * @param role
     * @param type
     * @param action
     */
    @SuppressWarnings("unchecked")
    public <T> void forEachConnection(String role, Class<T> type, Consumer<T> action) {
        Stream<Map.Entry<Connection<? extends Device>, List<String>>> stream = connections();

        if (role != null && !role.isEmpty()) {
            stream = stream.filter((Map.Entry<Connection<? extends Device>, List<String>> entry) -> entry.getValue().contains(role));
        }

        stream.filter((entry) -> type.isInstance(entry.getKey())).map((entry) -> (T) entry.getKey())
                .forEach(action);
    }

    public <T> void forEachConnection(Class<T> type, Consumer<T> action) {
        forEachConnection(null, type, action);
    }

    public Stream<Map.Entry<Connection<? extends Device>, List<String>>> connections() {
        return connections.entrySet().stream();
    }

    @Override
    protected void applyConfig(Meta config) {
        if (meta().hasValue("logger")) {
            setupLogger();
        }
        getLogger().debug("Applying configuration change");
        forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class, it -> it.notifyDeviceConfigChanged(AbstractDevice.this));
    }

    @Override
    public String type() {
        return meta().getString("type", "unknown");
    }
}

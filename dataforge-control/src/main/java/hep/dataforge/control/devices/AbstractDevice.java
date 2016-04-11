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

import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.control.connections.Connection;
import hep.dataforge.control.devices.annotations.RoleDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.BaseConfigurable;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ReferenceRegistry;
import hep.dataforge.values.Value;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
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

    private Context context;
    private String name;
    private final ReferenceRegistry<DeviceListener> listeners = new ReferenceRegistry<>();
    private final Map<Connection, List<String>> connections = new HashMap<>();
    private final Map<String, Value> states = new ConcurrentHashMap<>();
    private Logger logger;

    public void setContext(Context context) {
        this.context = context;
        setValueContext(context);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set fallback meta default for this device
     *
     * @param meta
     */
    public void setMeta(Meta meta) {
        setMetaBase(meta);
    }

    protected Logger setupLogger() {
        //TODO move logger construction to context IoManager
        return LoggerFactory.getLogger(getClass());
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = setupLogger();
            logger.error("Logger is not initialized. Call init() before working with device.");
        }
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
        getLogger().info("Shutting down device '{}'...", getName());
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
        if (context == null) {
            return GlobalContext.instance();
        } else {
            return this.context;
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return this.name;
        }
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
        this.states.put(stateName, stateValue);
        getLogger().info("State {} changed to {}", stateName, stateValue);
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
        getLogger().error(message, error);
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
    @Override
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
     * @throws hep.dataforge.exceptions.ControlException
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
     * @param connection
     * @param roles
     */
    @Override
    public synchronized void connect(Connection<Device> connection, String... roles) {
        getLogger().info("Attaching connection with roles {}", String.join(", ", roles));
        //Checking if connection could serve given roles
        for (String role : roles) {
            if (!hasRole(role)) {
                getLogger().warn("The device {} does not support role {}", getName(), role);
            } else {
                RoleDef rd = roleDefs().stream().filter((roleDef) -> roleDef.name().equals(name)).findAny().get();
                if (!rd.objectType().isInstance(connection)) {
                    getLogger().error("Connection does not meet type requirement for role {}. Must be {}.",
                            role, rd.objectType().getName());
                }
            }
        }
        this.connections.put(connection, Arrays.asList(roles));
        try {
            getLogger().debug("Opening connection...");
            connection.open(this);
        } catch (Exception ex) {
            //FIXME evaluate error here
            throw new Error(ex);
        }
    }

    /**
     * Perform some action for each connection with given role and/or connection
     * predicate. Both role and predicate could be empty
     *
     * @param role
     * @param predicate
     * @param action
     */
    public void forEachConnection(String role, Predicate<Connection> predicate, Consumer<Connection> action) {
        Stream<Map.Entry<Connection, List<String>>> stream = connections.entrySet().stream();

        if (role != null && !role.isEmpty()) {
            stream = stream.filter((Map.Entry<Connection, List<String>> entry) -> entry.getValue().contains(role));
        }

        if (predicate != null) {
            stream = stream.filter((Map.Entry<Connection, List<String>> entry) -> predicate.test(entry.getKey()));
        }

        stream.forEach((Map.Entry<Connection, List<String>> entry) -> action.accept(entry.getKey()));
    }

    /**
     * For each connection with given role
     *
     * @param role
     * @param action
     */
    public void forEachConnection(String role, Consumer<Connection> action) {
        AbstractDevice.this.forEachConnection(role, null, action);
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
    public <T> void forEachTypedConnection(String role, Class<T> type, Consumer<T> action) {
        Stream<Map.Entry<Connection, List<String>>> stream = connections.entrySet().stream();

        if (role != null && !role.isEmpty()) {
            stream = stream.filter((Map.Entry<Connection, List<String>> entry) -> entry.getValue().contains(role));
        }

        stream.filter((entry) -> type.isInstance(entry.getKey())).<T>map((entry) -> (T) entry.getKey())
                .forEach((con) -> action.accept(con));
    }

    @Override
    protected void applyConfig(Meta config) {
        getLogger().debug("Applying configuration change");
        listeners.forEach((DeviceListener it) -> it.notifyDeviceConfigChanged(AbstractDevice.this));
    }

    @Override
    public String type() {
        return "unknown";
    }
}

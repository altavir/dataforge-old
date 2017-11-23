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
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.utils.MetaHolder;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import static hep.dataforge.control.connections.Roles.DEVICE_LISTENER_ROLE;

/**
 * <p>
 * State has two components: physical and logical. If logical state does not
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
    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread res = new Thread(r);
            res.setName("device::" + getName());
            res.setPriority(Thread.MAX_PRIORITY);
            res.setDaemon(true);
            return res;
        }
    });

    public AbstractDevice(Context context, Meta meta) {
        super(meta);
        this.context = context;
        //initialize states
        stateDefs().stream()
                .filter(it -> !it.value().def().isEmpty())
                .forEach(it -> states.put(it.value().name(), Value.of(it.value().def())));
    }

    /**
     * A single thread executor for this device. All state changes and similar work must be done on this thread.
     *
     * @return
     */
    protected ExecutorService getExecutor() {
        return executor;
    }

    public Logger getLogger() {
        String loggerName = getMeta().getString("logger", () -> "device::" + getName());
        return LoggerFactory.getLogger(loggerName);
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

//    protected void setContext(Context context) {
//        this.context = context;
//    }

    @Override
    public String getName() {
        return getMeta().getString("name", getType());
    }

    /**
     * Update logical state and notify listeners.
     *
     * @param stateName
     * @param stateValue
     */
    private void notifyStateChanged(String stateName, Value stateValue) {
        executor.submit(() -> {
            this.states.put(stateName, stateValue);
            if (stateValue.isNull()) {
                getLogger().info("State {} is reset", stateName);
            } else {
                getLogger().info("State {} changed to {}", stateName, stateValue);
            }
            context.parallelExecutor().submit(() ->
                    forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                            it -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, stateValue))
            );
        });
    }

    /**
     * Post a value to the state and notify state changed if needed. Does not
     * change physical state
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
        context.parallelExecutor().submit(() ->
                forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                        it -> it.evaluateDeviceException(AbstractDevice.this, message, error))
        );
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
        executor.submit(() -> this.states.remove(stateName));
    }

    /**
     * Reset state to its default value if it is present
     */
    public final void resetState(String stateName) {
        executor.submit(() -> {
            this.states.remove(stateName);
            stateDefs().stream()
                    .filter(it -> Objects.equals(it.value().name(), stateName))
                    .findFirst()
                    .ifPresent(value -> states.put(stateName, Value.of(value)));
        });
    }

//    /**
//     * Force invalidate and immediately recalculate state
//     *
//     * @param stateName
//     */
//    protected final void recalculateState(String stateName) {
//        try {
//            updateState(stateName, computeState(stateName));
//        } catch (ControlException ex) {
//            notifyError("Can't calculate state " + stateName, ex);
//        }
//    }

    protected abstract Object computeState(String stateName) throws ControlException;

    protected final Value getDefaultState(String stateName) {
        return optStateDef(stateName).map(def -> def.value().def())
                .map(Value::of)
                .orElseThrow(() -> new RuntimeException("Can't calculate state " + stateName));
    }

    /**
     * Perform state change and return a value after change
     * @param stateName
     * @param value
     * @throws ControlException
     */
    protected abstract Object requestStateChange(String stateName, Value value) throws ControlException;

    /**
     * Request state change and update result
     * @param stateName
     * @param value
     */
    @Override
    public void setState(String stateName, Object value) {
        executor.submit(() -> {
            try {
                Value res = Value.of(requestStateChange(stateName, Value.of(value)));
                updateState(stateName, res);
            } catch (Exception e) {
                getLogger().error("Failed to set state {} to {} with exception: {}", stateName, value, e.toString());
            }
        });
    }

    public Future<Value> getStateInFuture(String stateName) {
        return executor.submit(() -> this.states.computeIfAbsent(stateName, (String t) -> {
            try {
                Value newState = Value.of(computeState(stateName));
                notifyStateChanged(stateName, newState);
                return newState;
            } catch (ControlException ex) {
                notifyError("Can't calculate state " + stateName, ex);
                return Value.NULL;
            }
        }));
    }

    @Override
    public Value getState(String stateName) {
        try {
            return getStateInFuture(stateName).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to calculate state " + stateName);
        }
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
        return getMeta().getString("type", "unknown");
    }
}

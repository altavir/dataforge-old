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
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;

import java.time.Duration;
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

    private final Map<String, Value> states = new HashMap<>();
    private final Map<String, Meta> metastates = new HashMap<>();

    private Context context;
    private ConnectionHelper connectionHelper;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread res = new Thread(r);
        res.setName("device::" + getName());
        res.setPriority(Thread.MAX_PRIORITY);
        res.setDaemon(true);
        return res;
    });

    public AbstractDevice(Context context, Meta meta) {
        super(meta);
        this.context = context;
        //initialize states
        getStateDefs().stream()
                .filter(it -> !it.value().def().isEmpty())
                .forEach(it -> states.put(it.value().name(), Value.of(it.value().def())));
    }

    /**
     * A single thread executor for this device. All state changes and similar work must be done on this thread.
     *
     * @return
     */
    protected ScheduledExecutorService getExecutor() {
        return executor;
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
        updateLogicalState(INITIALIZED_STATE, true);
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
        updateLogicalState(INITIALIZED_STATE, false);
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

    @Override
    public String getName() {
        return getMeta().getString("name", getType());
    }

    protected void execute(Runnable runnable) {
        executor.submit(runnable);
    }

    protected <T> Future<T> execute(Callable<T> runnable) {
        return executor.submit(runnable);
    }

    protected ScheduledFuture<?> schedule(Duration delay, Runnable runnable) {
        return executor.schedule(runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Update logical state if it is changed
     *
     * @param stateName
     * @param stateValue
     */
    protected void updateLogicalState(String stateName, Object stateValue) {
        if (stateValue instanceof Meta) {
            updateLogicalMetaState(stateName, (Meta) stateValue);
        } else {
            Value oldState = this.states.get(stateName);
            Value newState = Value.of(stateValue);
            //Notify only if state really changed
            if (!newState.equals(oldState)) {
                //Update logical state and notify listeners.
                execute(() -> {
                    this.states.put(stateName, newState);
                    if (newState.isNull()) {
                        getLogger().info("State {} is reset", stateName);
                    } else {
                        getLogger().info("State {} changed to {}", stateName, newState);
                    }
                    context.parallelExecutor().submit(() -> {
                                forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                                        it -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, newState));
                            }
                    );
                });
            }
        }
    }

    protected void updateLogicalMetaState(String stateName, Meta metaStateValue) {
        Meta oldState = this.metastates.get(stateName);
        //Notify only if state really changed
        if (!metaStateValue.equals(oldState)) {
            //Update logical state and notify listeners.
            execute(() -> {
                this.metastates.put(stateName, metaStateValue);
                if (metaStateValue.isEmpty()) {
                    getLogger().info("Metastate {} is reset", stateName);
                } else {
                    getLogger().info("Metastate {} changed to {}", stateName, metaStateValue);
                }
                context.parallelExecutor().submit(() -> {
                            forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                                    it -> it.notifyDeviceStateChanged(AbstractDevice.this, stateName, metaStateValue));
                        }
                );
            });
        }
    }

    protected final void notifyError(String message, Throwable error) {
        getLogger().error(message, error);
        context.parallelExecutor().submit(() -> {
                    forEachConnection(DEVICE_LISTENER_ROLE, DeviceListener.class,
                            it -> it.evaluateDeviceException(AbstractDevice.this, message, error));
                }
        );
    }

    protected final void dispatchEvent(Event event) {
        forEachConnection(EventHandler.class, it -> it.pushEvent(event));
    }

    /**
     * Reset state to its default value if it is present
     */
    public final void resetState(String stateName) {
        execute(() -> {
            this.states.remove(stateName);
            getStateDefs().stream()
                    .filter(it -> Objects.equals(it.value().name(), stateName))
                    .findFirst()
                    .ifPresent(value -> states.put(stateName, Value.of(value)));
        });
    }

    /**
     * Get logical state
     *
     * @param stateName
     * @return
     */
    protected final Value getLogicalState(String stateName) {
        return Optionals.either(Optional.ofNullable(states.get(stateName)))
                .or(optStateDef(stateName).map(def -> def.value().def()).map(Value::of))
                .opt()
                .orElseThrow(() -> new RuntimeException("Can't calculate state " + stateName));
    }

    /**
     * Request the change of physical and/or logical state.
     *
     * @param stateName
     * @param value
     * @throws ControlException
     */
    protected abstract void requestStateChange(String stateName, Value value) throws ControlException;

    /**
     * Request the change of physical ano/or logical meta state.
     * @param stateName
     * @param meta
     * @throws ControlException
     */
    protected abstract void requestMetaStateChange(String stateName, Meta meta) throws ControlException;


    /**
     * Compute physical state
     *
     * @param stateName
     * @return
     * @throws ControlException
     */
    protected abstract Object computeState(String stateName) throws ControlException;

    /**
     * Compute phisical meta state
     * @param stateName
     * @return
     * @throws ControlException
     */
    protected abstract Meta computeMetaState(String stateName) throws ControlException;

    /**
     * Request state change and update result
     *
     * @param stateName
     * @param value
     */
    @Override
    public void setState(String stateName, Object value) {
        execute(() -> {
            try {
                requestStateChange(stateName, Value.of(value));
            } catch (Exception e) {
                getLogger().error("Failed to set state {} to {} with exception: {}", stateName, value, e.toString());
            }
        });
    }

    @Override
    public void setMetaState(String stateName, Meta meta) {
        execute(() -> {
            try {
                requestMetaStateChange(stateName, meta);
            } catch (Exception e) {
                getLogger().error("Failed to set  metastate {} to {} with exception: {}", stateName, meta, e.toString());
            }
        });
    }

    public Future<Value> getStateInFuture(String stateName) {
        return execute(() -> this.states.computeIfAbsent(stateName, (String t) ->
                        states.computeIfAbsent(stateName, state -> {
                            try {
                                return Value.of(computeState(stateName));
                            } catch (ControlException ex) {
                                notifyError("Can't calculate state " + stateName, ex);
                                return Value.NULL;
                            }
                        })
                )
        );
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

    public Future<Meta> getMetaStateInFuture(String stateName) {
        return execute(() -> this.metastates.computeIfAbsent(stateName, (String t) ->
                        metastates.computeIfAbsent(stateName, state -> {
                            try {
                                return computeMetaState(stateName);
                            } catch (ControlException ex) {
                                notifyError("Can't calculate metastate " + stateName, ex);
                                return Meta.empty();
                            }
                        })
                )
        );
    }

    @Override
    public Optional<Meta> optMetaState(String stateName) {
        if (states.containsKey(stateName)) {
            return Optional.of(metastates.get(stateName));
        } else {
            return Device.super.optMetaState(stateName);
        }
    }

    @Override
    public Meta getMetaState(String stateName) {
        try {
            return getMetaStateInFuture(stateName).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to calculate metastate " + stateName);
        }
    }

    @Override
    public String getType() {
        return getMeta().getString("type", "unknown");
    }
}

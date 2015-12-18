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

import ch.qos.logback.classic.Logger;
import hep.dataforge.context.Context;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ReferenceRegistry;
import hep.dataforge.values.Value;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class AbstractDevice implements Device {

    private Logger logger;
    protected final String name;
    protected final Context context;
    protected Meta annotation;
    private final ReferenceRegistry<DeviceListener> listeners = new ReferenceRegistry<>();
    protected ScheduledExecutorService executor;
    protected final Map<String, Value> stateMap = new ConcurrentHashMap<>();

    public AbstractDevice(String name, Context context, Meta annotation) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }

        this.name = name;
        this.context = context;
        this.annotation = annotation;
        this.logger = (Logger) LoggerFactory.getLogger(name);
    }

    @Override
    public void init() throws ControlException {
//        logger.info("Initializing device '{}'...", getName());
        this.executor = buildExecutor();
        listeners.forEach(it -> it.notifyDeviceInitialized(this));
    }

    @Override
    public void shutdown() throws ControlException {
//        logger.info("Shutting down device '{}'...", getName());
        this.executor = null;
        listeners.forEach(it -> it.notifyDeviceShutdown(this));
    }

    protected ScheduledExecutorService buildExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public Value getState(String name) {
        return this.stateMap.get(name);
    }

    /**
     * Notify device that one of it states is changed. If the state not present,
     * it is created.
     *
     * @param name
     * @param value
     */
    protected void setState(String name, Object value) {
        Value oldValue = this.stateMap.get(name);
        Value newValue = Value.of(value);
        if (!newValue.equals(oldValue)) {// ignoring change is state not changed
            this.stateMap.put(name, newValue);
            listeners.forEach(it -> it.notifyDeviceStateChanged(this, name, oldValue, newValue));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta meta() {
        return annotation != null ? annotation : Meta.buildEmpty("device");
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Envelope respond(Envelope message) {
        //TODO some general device logic here
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    protected void sendMessage(int priority, Meta message) {
//        listeners.forEach(it -> it.sendMessage(this, priority, message));
//    }

    @Override
    public void addDeviceListener(DeviceListener listener) {
        this.listeners.add(listener);
    }

    public Logger getLogger() {
        return logger;
    }

}

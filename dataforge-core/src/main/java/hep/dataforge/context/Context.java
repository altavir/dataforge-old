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
package hep.dataforge.context;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.reports.Log;
import hep.dataforge.io.reports.Logable;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import hep.dataforge.providers.AbstractProvider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * The local environment for anything being done in DataForge framework. Contexts are organized into tree structure with {@link Global} at the top.
 * </p>
 * <p>
 * Each context has a set of named {@link Value} properties which are taken from parent context in case they are not found in local context. Context implements {@link ValueProvider} interface and therefore could be uses as a value source for substitutions etc.
 * </p>
 * <p>
 * Context contains {@link PluginManager} which could be used any number of configurable named plugins.
 * </p>
 * <p>
 * Also Context has its own logger and {@link IOManager} to govern all the input and output being made inside the context.
 * </p>
 *
 * @author Alexander Nozik
 */
public class Context extends AbstractProvider implements ValueProvider, Logable, Named, AutoCloseable {

    protected final Map<String, Value> properties = new ConcurrentHashMap<>();
    private final String name;
    private final PluginManager pm;
    protected Logger logger;
    protected Log rootLog;
    protected IOManager io = null;
    private Context parent = null;

    protected ExecutorService parallelExecutor;
    protected ExecutorService singleThreadExecutor;


    /**
     * Build context from metadata
     *
     * @param name
     */
    protected Context(String name) {
        this.pm = new PluginManager(this);
        this.rootLog = new Log(name);
        this.name = name;
    }

    public Context withParent(Context parent) {
        this.parent = parent;
        rootLog.setParent(parent);
        return this;
    }

    public Context withProperties(Meta config) {
        if (config != null) {
            if (config.hasMeta("property")) {
                config.getMetaList("property").stream().forEach((propertyNode) -> {
                    properties.put(propertyNode.getString("key"), propertyNode.getValue("value"));
                });
            }
        }
        return this;
    }

    public Logger getLogger() {
        if (logger == null) {
            return LoggerFactory.getLogger(getName());
        } else {
            return logger;
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
        startLoggerAppender();
    }

    /**
     * {@inheritDoc} namespace does not work
     */
    @Override
    public Value getValue(String path) {
        Value res = properties.get(path);
        if (res != null) {
            return res;
        } else if (parent != null) {
            return parent.getValue(path);
        } else {
            throw new NameNotFoundException(path);
        }
    }

    /**
     * {@inheritDoc} namespace does not work
     */
    @Override
    public boolean hasValue(String path) {
        return properties.containsKey(path) || (parent != null && parent.hasValue(path));
    }

    /**
     * Return parent context or global context if parent is not defined
     *
     * @return a {@link hep.dataforge.context.Context} object.
     */
    public Context getParent() {
        if (this.parent != null) {
            return parent;
        } else {
            return Global.instance();
        }
    }

    /**
     * Return IO manager of this context. By default parent IOManager is
     * returned.
     *
     * @return the io
     */
    public IOManager io() {
        if (io == null) {
            return parent.io();
        } else {
            return io;
        }
    }

    /**
     * Set IO manager for this context
     *
     * @param io
     */
    public synchronized void setIO(IOManager io) {
        //detaching old io manager
        if (this.io != null) {
            stopLoggerAppender();
            this.io.detach();
        }

        io.attach(this);
        this.io = io;
        getLog().addListener(io().getLogEntryHandler());
        startLoggerAppender();
    }

    private void startLoggerAppender() {
        if (getLogger() instanceof ch.qos.logback.classic.Logger) {
            io().addLoggerAppender((ch.qos.logback.classic.Logger) getLogger());
        }
    }

    private void stopLoggerAppender() {
        if (getLogger() instanceof ch.qos.logback.classic.Logger) {
            io().removeLoggerAppender((ch.qos.logback.classic.Logger) getLogger());
        }
    }

    /**
     * Plugin manager for this Context
     *
     * @return
     */
    public final PluginManager pluginManager() {
        return this.pm;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Check if object is provided in this namespace as is
     *
     * @param <T>
     * @param target a {@link java.lang.String} object.
     * @param name   a {@link hep.dataforge.names.Name} object.
     * @return a boolean.
     */
    @SuppressWarnings("unchecked")
    public <T> T provideInNameSpace(String target, Name name) {
        if ("value".equals(target)) {
            return (T) getValue(name.toString());
        } else if (target.isEmpty() || "plugin".equals(target)) {
            return (T) pluginManager().getPlugin(name.toString());
        } else {
            throw new TargetNotProvidedException();
        }
    }

    /**
     * Provide an object from namespace as is (without namespace substitution)
     *
     * @param target
     * @param name
     * @return
     */
    public boolean providesInNameSpace(String target, Name name) {
        if ("value".equals(target)) {
            return hasValue(name.toString());
        } else if (target.isEmpty() || "plugin".equals(target)) {
            return pluginManager().hasPlugin(name.toString());
        } else {
            return false;
        }
    }

    /**
     * <p>
     * putValue.</p>
     *
     * @param name  a {@link java.lang.String} object.
     * @param value a {@link hep.dataforge.values.Value} object.
     */
    public void putValue(String name, Value value) {
        properties.put(name, value);
    }

    public void putValue(String name, Object value) {
        properties.put(name, Value.of(value));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Log getLog() {
        return this.rootLog;
    }

    @Override
    public boolean provides(String target, Name name) {
        return providesInNameSpace(target, name)
                || (name.nameSpace().equals(getName()) && providesInNameSpace(target, name.toNameSpace("")));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Object provide(String target, Name name) {
        if (providesInNameSpace(target, name)) {
            return provideInNameSpace(target, name);
        } else if (name.nameSpace().equals(getName())) {
            return provideInNameSpace(target, name.toNameSpace(""));
        } else {
            throw new NameNotFoundException(name.toString());
        }
    }

    public Map<String, Value> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Free up resources associated with this context
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        //detach all plugins
        pluginManager().close();

        //stopping all works in this context
        if (parallelExecutor != null) {
            parallelExecutor.shutdown();
            parallelExecutor = null;
        }

        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdown();
            singleThreadExecutor = null;
        }
    }

    /**
     * Get parallelExecutor for given process name. By default uses one thread
     * pool parallelExecutor for all processes
     *
     * @return
     */
    public ExecutorService parallelExecutor() {
        if (this.parallelExecutor == null) {
            getLogger().info("Initializing parallel executor in {}", getName());
            this.parallelExecutor = Executors.newWorkStealingPool();
        }
        return parallelExecutor;
    }

    /**
     * An executor for tasks that do not allow parallelization
     *
     * @return
     */
    public ExecutorService singleThreadExecutor() {
        if (this.singleThreadExecutor == null) {
            getLogger().info("Initializing single thread executor in {}", getName());
            this.singleThreadExecutor = Executors.newSingleThreadExecutor(r -> {
                        Thread thread = new Thread(r);
                        thread.setDaemon(false);
                        thread.setName(getName() + "_single");
                        return thread;
                    }
            );
        }
        return singleThreadExecutor;
    }

    /**
     * Get typed plugin by its class
     *
     * @param type
     * @param <T>
     * @return
     */
    //TODO move to utils
    public <T> T getPlugin(Class<T> type) {
        return pluginManager()
                .stream(true)
                .filter(it -> type.isInstance(it))
                .findFirst()
                .map(it -> type.cast(it))
                .orElseThrow(() -> new RuntimeException("Plugin could not be loaded by type: " + type.getName()));

    }

}

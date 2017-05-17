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

import hep.dataforge.io.BasicIOManager;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.history.Chronicle;
import hep.dataforge.io.history.History;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hep.dataforge.io.history.Chronicle.CHRONICLE_TARGET;

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
public class Context implements Provider, ValueProvider, History, Named, AutoCloseable {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Builder builder(String name, Context parent) {
        return new Builder(name, parent);
    }

    protected final Map<String, Value> properties = new ConcurrentHashMap<>();
    private final String name;
    private final PluginManager pm;
    protected Logger logger;
    protected Chronicle rootLog;
    private ClassLoader classLoader = null;

    //TODO move to separate manager
    private transient Map<String, Chronicle> historyCache = new HashMap<>();

    protected ExecutorService parallelExecutor;
    protected ExecutorService singleThreadExecutor;
    private Context parent = null;


    /**
     * Build context from metadata
     *
     * @param name
     */
    protected Context(String name) {
        this.pm = new PluginManager(this);
        this.rootLog = new Chronicle(name);
        this.name = name;
    }

    /**
     * Get the class loader for this context
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        if (this.classLoader != null) {
            return this.classLoader;
        } else {
            return getParent().getClassLoader();
        }
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
    public Optional<Value> optValue(String path) {
        Optionals<Value> opts = Optionals.either(Optional.ofNullable(properties.get(path)));
        if (getParent() != null) {
            opts = opts.or(() -> getParent().optValue(path));
        }
        return opts.opt();
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
        return pluginManager().opt(IOManager.class).orElseGet(() -> Global.instance().io());
    }

    private void startLoggerAppender() {
        if (getLogger() instanceof ch.qos.logback.classic.Logger) {
            io().addLoggerAppender((ch.qos.logback.classic.Logger) getLogger());
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

    @Override
    public String defaultTarget() {
        return "plugin";
    }

    @Provides(Plugin.PLUGIN_TARGET)
    public Optional<Plugin> optPlugin(String pluginName) {
        return pluginManager().opt(PluginTag.fromString(pluginName));
    }

    @ProvidesNames(Plugin.PLUGIN_TARGET)
    public Collection<String> listPluigns() {
        return pluginManager().list().stream().map(Plugin::getName).collect(Collectors.toSet());
    }

    @ProvidesNames(VALUE_TARGET)
    public Collection<String> listValues() {
        return properties.keySet();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Chronicle getLog() {
        return this.rootLog;
    }

    @Provides(CHRONICLE_TARGET)
    public Optional<Chronicle> optChronicle(String logName) {
        return Optional.ofNullable(historyCache.get(logName));
    }

    /**
     * get or build current log creating the whole log hierarchy
     *
     * @param reportName
     * @return
     */
    public Chronicle getChronicle(String reportName) {
        return historyCache.computeIfAbsent(reportName, str -> {
            Name name = Name.of(str);
            History parent;
            if (name.length() > 1) {
                parent = getChronicle(name.cutLast().toString());
            } else {
                parent = Context.this;
            }
            return new Chronicle(name.getLast().toString(), parent);
        });
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
     * Get a plugin extending given class
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getFeature(Class<T> type) {
        return optFeature(type)
                .orElseThrow(() -> new RuntimeException("Feature could not be loaded by type: " + type.getName()));
    }

    /**
     * Opt a plugin extending given class
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> Optional<T> optFeature(Class<T> type) {
        return pluginManager()
                .stream(true)
                .filter(it -> type.isInstance(it))
                .findFirst()
                .map(it -> type.cast(it));
    }

    /**
     * Get stream of services of given class provided by Java SPI or any other service loading API.
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public synchronized <T> Stream<T> serviceStream(Class<T> serviceClass) {
        return StreamSupport.stream(ServiceLoader.load(serviceClass, getClassLoader()).spliterator(), false);
    }

    /**
     *
     * @param serviceClass
     * @param predicate
     * @param <T>
     * @return
     */
    public <T> Optional<T> findService(Class<T> serviceClass, Predicate<T> predicate) {
        return serviceStream(serviceClass).filter(predicate).findFirst();
    }

    public static class Builder {
        Context ctx;

        public Builder(String name) {
            this.ctx = Global.getContext(name);
        }

        public Builder(String name, Context parent) {
            this.ctx = Global.getContext(Name.joinString(parent.getName(), name));
            this.ctx.parent = parent;
        }

        public Builder properties(Meta config) {
            if (config != null) {
                if (config.hasMeta("property")) {
                    config.getMetaList("property").stream().forEach((propertyNode) -> {
                        ctx.putValue(propertyNode.getString("key"), propertyNode.getValue("value"));
                    });
                }
            }
            return this;
        }

        /**
         * Load and configure a plugin
         *
         * @param type
         * @param configuration
         * @return
         */
        public Builder plugin(Class<? extends Plugin> type, Meta configuration) {
            ctx.pluginManager().load(type, pl -> pl.configure(configuration));
            return this;
        }

        public <T extends Plugin> Builder plugin(Class<T> type, Consumer<T> initializer) {
            ctx.pluginManager().load(type, initializer);
            return this;
        }

        public Builder plugin(Class<? extends Plugin> type) {
            ctx.pluginManager().load(type);
            return this;
        }

        public Builder classPath(URL... path) {
            ctx.classLoader = new URLClassLoader(path, ctx.getParent().getClassLoader());
            return this;
        }

        /**
         * Create new IO manager for this context if needed (using default input and output of parent) and set its root
         *
         * @param rootDir
         * @return
         */
        public Builder setRootDir(File rootDir) {
            if (!ctx.pluginManager().opt(IOManager.class).isPresent()) {
                ctx.pluginManager().load(new BasicIOManager(ctx.getParent().io().in(), ctx.getParent().io().out()));
            }
            ctx.putValue(IOManager.ROOT_DIRECTORY_CONTEXT_KEY, rootDir.getAbsoluteFile());
            return this;
        }


        public Context build() {
            return ctx;
        }
    }


}

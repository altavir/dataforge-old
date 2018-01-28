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

import hep.dataforge.cache.Identifiable;
import hep.dataforge.io.BasicIOManager;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.history.Chronicle;
import hep.dataforge.io.history.History;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
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
public class Context implements Provider, ValueProvider, History, Named, AutoCloseable, Identifiable {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Builder builder(String name, Context parent) {
        return new Builder(parent, name);
    }

    protected final Map<String, Value> properties = new ConcurrentHashMap<>();
    private final String name;
    private final PluginManager pm;
    protected Logger logger;
    private Chronicle rootLog;
    private ClassLoader classLoader = null;
    private final ContextLock lock = new ContextLock(this);

    //TODO move to separate plugin
    private transient Map<String, Chronicle> historyCache = new HashMap<>();

    //TODO move to plugin
    private ExecutorService parallelExecutor;
    private ExecutorService singleThreadExecutor;
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

    public Logger getLogger(String sub) {
        return LoggerFactory.getLogger(getName() + "[" + sub + "]");
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
    public IOManager getIo() {
        return getPluginManager().opt(IOManager.class).orElseGet(() -> getParent().getIo());
    }


    /**
     * Plugin manager for this Context
     *
     * @return
     */
    @NotNull
    public final PluginManager getPluginManager() {
        return this.pm;
    }

    /**
     * The name of the context
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Add property to context
     *
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        lock.modify(() -> {
            properties.put(name, Value.of(value));
        });
    }

    @Override
    public String defaultTarget() {
        return "plugin";
    }

    @Provides(Plugin.PLUGIN_TARGET)
    public Optional<Plugin> optPlugin(String pluginName) {
        return getPluginManager().opt(PluginTag.fromString(pluginName));
    }

    @ProvidesNames(Plugin.PLUGIN_TARGET)
    public Collection<String> listPlugins() {
        return getPluginManager().list().stream().map(Plugin::getName).collect(Collectors.toSet());
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
    public Chronicle getChronicle() {
        return this.rootLog;
    }

    @Provides(CHRONICLE_TARGET)
    public Optional<Chronicle> optChronicle(String logName) {
        return Optional.ofNullable(historyCache.get(logName));
    }

    /**
     * get or builder current log creating the whole log hierarchy
     *
     * @param reportName
     * @return
     */
    public Chronicle getChronicle(String reportName) {
        return historyCache.computeIfAbsent(reportName, str -> {
            Name name = Name.of(str);
            History parent;
            if (name.getLength() > 1) {
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
        getPluginManager().close();

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
     * Get parallel executor for given process name. By default uses one thread pool parallel executor for all processes
     *
     * @return
     */
    public ExecutorService getParallelExecutor() {
        if (this.parallelExecutor == null) {
            getLogger().info("Initializing parallel executor in {}", getName());
            ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName(getName() + "_worker-" + worker.getPoolIndex());
                return worker;
            };
            this.parallelExecutor = new ForkJoinPool(
                    Runtime.getRuntime().availableProcessors(),
                    factory,
                    null, false);
        }
        return parallelExecutor;
    }

    /**
     * A dispatch thread executor for current context
     *
     * @return
     */
    public ExecutorService getDispatcher() {
        if (this.singleThreadExecutor == null) {
            getLogger().info("Initializing dispatch thread executor in {}", getName());
            this.singleThreadExecutor = Executors.newSingleThreadExecutor(r -> {
                        Thread thread = new Thread(r);
                        thread.setPriority(8); // slightly higher priority
                        thread.setDaemon(true);
                        thread.setName(getName() + "_dispatch");
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

    public <T> T loadFeature(String tag, Class<T> type) {
        return type.cast(getPluginManager().getOrLoad(tag, Meta.empty()));
    }


    /**
     * Opt a plugin extending given class
     *
     * @param type
     * @param <T>
     * @return
     */
    @NotNull
    public <T> Optional<T> optFeature(Class<T> type) {
        return getPluginManager()
                .stream(true)
                .filter(type::isInstance)
                .findFirst()
                .map(type::cast);
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
     * @param serviceClass
     * @param predicate
     * @param <T>
     * @return
     */
    public <T> Optional<T> findService(Class<T> serviceClass, Predicate<T> predicate) {
        return serviceStream(serviceClass).filter(predicate).findFirst();
    }

    /**
     * Get identity for this context
     *
     * @return
     */
    @Override
    public Meta getIdentity() {
        MetaBuilder id = new MetaBuilder("context");
        id.update(properties);
        getPluginManager().stream(true).forEach(plugin -> {
            if (plugin.getClass().isAnnotationPresent(PluginDef.class)) {
                if (!plugin.getClass().getAnnotation(PluginDef.class).support()) {
                    id.putNode(plugin.getIdentity());
                }

            }
        });
        return id;
    }

    /**
     * Lock this context by given object
     *
     * @param obj
     */
    public void lock(Object obj) {
        this.lock.lock(obj);
        if (getParent() != null) {
            getParent().lock(obj);
        }
    }

    /**
     * Unlock the context by given object
     *
     * @param obj
     */
    public void unlock(Object obj) {
        this.lock.unlock(obj);
        if (getParent() != null) {
            getParent().unlock(obj);
        }
    }

    /**
     * Find out if context is locked
     *
     * @return
     */
    public boolean isLocked() {
        return lock.isLocked();
    }

    /**
     * A builder for context
     */
    public static class Builder {
        private Context ctx;

        private List<URL> classPath = new ArrayList<>();

        public Builder(String name) {
            this.ctx = Global.getContext(name);
        }

        public Builder(Context parent, String name) {
            this.ctx = Global.getContext(Name.joinString(parent.getName(), name));
            this.ctx.parent = parent;
        }

        public Builder properties(@NotNull Meta config) {
            if (config.hasMeta("property")) {
                config.getMetaList("property").forEach((propertyNode) -> {
                    ctx.setValue(propertyNode.getString("key"), propertyNode.getValue("value"));
                });
            } else if (Objects.equals(config.getName(), "properties")) {
                MetaUtils.valueStream(config).forEach(pair -> ctx.setValue(pair.getKey(), pair.getValue()));
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
            ctx.getPluginManager().load(type, configuration);
            return this;
        }

        public Builder plugin(String type, Meta configuration) {
            ctx.getPluginManager().load(type, configuration);
            return this;
        }

        public Builder plugin(Class<? extends Plugin> type) {
            ctx.getPluginManager().load(type);
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder plugin(Meta meta) {
            Meta plMeta = meta.getMetaOrEmpty(MetaBuilder.DEFAULT_META_NAME);
            if (meta.hasValue("name")) {
                return plugin(meta.getString("name"), plMeta);
            } else if (meta.hasValue("class")) {
                Class<? extends Plugin> type = null;
                try {
                    type = (Class<? extends Plugin>) Class.forName(meta.getString("class"));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize plugin from meta", e);
                }
                return plugin(type, plMeta);
            } else {
                throw new IllegalArgumentException("Malformed plugin definition");
            }
        }

        public Builder classPath(URL... path) {
            classPath.addAll(Arrays.asList(path));
            return this;
        }

        public Builder classPath(URI path) {
            try {
                classPath.add(path.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Malformed classpath");
            }
            return this;
        }

        /**
         * Create additional classpath from a list of strings
         *
         * @param pathStr
         * @return
         */
        public Builder classPath(String pathStr) {
            Path path = Paths.get(pathStr);
            if (Files.isDirectory(path)) {
                try {
                    Files.find(path, -1, (subPath, basicFileAttributes) -> subPath.toString().endsWith(".jar"))
                            .map(Path::toUri).forEach(this::classPath);
                    return this;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load library", e);
                }
            } else if (Files.exists(path)) {
                return classPath(path.toUri());
            } else {
                return this;
            }

        }

        public Builder classPath(Collection<URL> paths) {
            classPath.addAll(paths);
            return this;
        }

        /**
         * Create new IO manager for this context if needed (using default input and output of parent) and set its root
         *
         * @param rootDir
         * @return
         */
        public Builder setRootDir(String rootDir) {
            if (!ctx.getPluginManager().opt(IOManager.class).isPresent()) {
                ctx.getPluginManager().load(new BasicIOManager(ctx.getParent().getIo().in(), ctx.getParent().getIo().out()));
            }
            ctx.setValue(IOManager.ROOT_DIRECTORY_CONTEXT_KEY, rootDir);
            return this;
        }

        public String getRootDir() {
            return ctx.optString(IOManager.ROOT_DIRECTORY_CONTEXT_KEY).orElseGet(() -> ctx.getIo().getRootDirectory().toString());
        }

        public Builder setDataDir(String rootDir) {
            if (!ctx.getPluginManager().opt(IOManager.class).isPresent()) {
                ctx.getPluginManager().load(new BasicIOManager(ctx.getParent().getIo().in(), ctx.getParent().getIo().out()));
            }
            ctx.setValue(IOManager.DATA_DIRECTORY_CONTEXT_KEY, rootDir);
            return this;
        }

        public String getDataDir() {
            return ctx.optString(IOManager.DATA_DIRECTORY_CONTEXT_KEY).orElseGet(() -> ctx.getIo().getRootDirectory().toString());
        }

        public Context build() {
            // automatically add lib directory
            ctx.getIo().optFile("lib").ifPresent(file -> classPath(file.toUri()));
            ctx.classLoader = new URLClassLoader(classPath.toArray(new URL[classPath.size()]), ctx.getParent().getClassLoader());
            return ctx;
        }
    }

    /**
     * Build a new context based on given meta
     *
     * @param name
     * @param parent
     * @param meta
     * @return
     */
    public static Context build(String name, Context parent, Meta meta) {
        Context.Builder builder = builder(name, parent);

        meta.optMeta("properties").ifPresent(builder::properties);

        meta.optString("rootDir").ifPresent(builder::setRootDir);

        meta.optValue("classpath").ifPresent(value ->
                value.listValue().stream().map(Value::stringValue).forEach(builder::classPath)
        );

        meta.getMetaList("plugin").forEach(builder::plugin);

        return builder.build();
    }

    public static Context build(Context parent, Meta meta) {
        return build(meta.getString("name"), parent, meta);
    }

    public static Context build(Meta meta) {
        return build(Global.instance(), meta);
    }

}

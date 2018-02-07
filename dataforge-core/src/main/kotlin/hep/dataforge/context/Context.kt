/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.context

import hep.dataforge.io.history.Chronicle
import hep.dataforge.io.history.History
import hep.dataforge.kodex.buildMeta
import hep.dataforge.kodex.nullable
import hep.dataforge.kodex.optional
import hep.dataforge.kodex.useMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaID
import hep.dataforge.names.Named
import hep.dataforge.providers.Provider
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass

/**
 *
 *
 * The local environment for anything being done in DataForge framework. Contexts are organized into tree structure with [Global] at the top.
 * Each context has a set of named [Value] properties which are taken from parent context in case they are not found in local context.
 * Context implements [ValueProvider] interface and therefore could be uses as a value source for substitutions etc.
 * Context contains [PluginManager] which could be used any number of configurable named plugins.
 * Also Context has its own logger and [IOManager] to govern all the input and output being made inside the context.
 * @author Alexander Nozik
 */
open class Context(
        private val name: String,
        val parent: Context? = Global,
        classLoader: ClassLoader? = null) : Provider, ValueProvider, History, Named, AutoCloseable, MetaID {

    /**
     * A class loader for this context. Parent class loader is used by default
     */
    open val classLoader: ClassLoader = classLoader ?: parent?.classLoader ?: Global.classLoader

    private val properties: MutableMap<String, Value> = ConcurrentHashMap()
    /**
     * Plugin manager for this Context
     *
     * @return
     */
    val pluginManager: PluginManager by lazy { PluginManager(this) }
    var logger: Logger = LoggerFactory.getLogger(name)
    private val lock by lazy { ContextLock(this) }

    /**
     * Return IO manager of this context. By default parent IOManager is
     * returned.
     *
     * @return the io
     */
    open val io: IOManager
        get() = pluginManager.get(IOManager::class) ?: parent?.io ?: Global.io


    /**
     * A property showing that dispatch thread is started in the context
     */
    private var started = false

    /**
     * A dispatch thread executor for current context
     *
     * @return
     */
    val dispatcher: ExecutorService by lazy {
        logger.info("Initializing dispatch thread executor in {}", getName())
        Executors.newSingleThreadExecutor { r ->
            Thread(r).apply {
                priority = 8 // slightly higher priority
                isDaemon = true
                name = this@Context.name + "_dispatch"
            }.also { started = true }
        }
    }

    open val executor: ExecutorPlugin
        get() = pluginManager.get(ExecutorPlugin::class) ?: parent?.executor ?: Global.executor

    /**
     * Find out if context is locked
     *
     * @return
     */
    val isLocked: Boolean
        get() = lock.isLocked

    open val history: Chronicler
        get() = pluginManager.get(Chronicler::class) ?: parent?.history ?: Global.history

    override fun getChronicle(): Chronicle {
        return history.chronicle
    }

    /**
     * {@inheritDoc} namespace does not work
     */
    override fun optValue(path: String): Optional<Value> {
        return (properties[path] ?: parent?.optValue(path).nullable).optional
    }

    /**
     * The name of the context
     *
     * @return
     */
    override fun getName(): String {
        return name
    }

    /**
     * Add property to context
     *
     * @param name
     * @param value
     */
    fun setValue(name: String, value: Any) {
        lock.modify { properties[name] = Value.of(value) }
    }

    override fun defaultTarget(): String {
        return Plugin.PLUGIN_TARGET
    }

    @Provides(Plugin.PLUGIN_TARGET)
    fun optPlugin(pluginName: String): Optional<Plugin> {
        return pluginManager.get(PluginTag.fromString(pluginName)).optional
    }

    @ProvidesNames(Plugin.PLUGIN_TARGET)
    fun listPlugins(): Collection<String> {
        return pluginManager.list().map { it.name }
    }

    @ProvidesNames(ValueProvider.VALUE_TARGET)
    fun listValues(): Collection<String> {
        return properties.keys
    }


    fun getProperties(): Map<String, Value> {
        return Collections.unmodifiableMap(properties)
    }


    /**
     * Get a plugin extending given class
     *
     * @param type
     * @param <T>
     * @return
     */
    operator fun <T> get(type: Class<T>): T {
        return optFeature(type)
                .orElseThrow { RuntimeException("Feature could not be loaded by type: " + type.name) }
    }

    fun <T : Plugin> load(type: Class<T>, meta: Meta = Meta.empty()): T {
        return pluginManager.load(type, meta)
    }

    fun <T : Plugin> load(type: KClass<T>, meta: Meta = Meta.empty()): T {
        return pluginManager.load(type, meta)
    }


    /**
     * Opt a plugin extending given class
     *
     * @param type
     * @param <T>
     * @return
     */
    fun <T> optFeature(type: Class<T>): Optional<T> {
        return pluginManager
                .stream(true)
                .filter { type.isInstance(it) }
                .findFirst()
                .map { type.cast(it) }
    }

    /**
     * Get stream of services of given class provided by Java SPI or any other service loading API.
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    @Synchronized
    fun <T> serviceStream(serviceClass: Class<T>): Stream<T> {
        return StreamSupport.stream(ServiceLoader.load(serviceClass, classLoader).spliterator(), false)
    }

    /**
     * Find specific service provided by java SPI
     *
     * @param serviceClass
     * @param predicate
     * @param <T>
     * @return
     */
    fun <T> findService(serviceClass: Class<T>, predicate: Predicate<T>): Optional<T> {
        return serviceStream(serviceClass).filter(predicate).findFirst()
    }

    /**
     * Get identity for this context
     *
     * @return
     */
    override fun toMeta(): Meta {
        return buildMeta("context") {
            update(properties)
            pluginManager.stream(true).forEach { plugin ->
                if (plugin.javaClass.isAnnotationPresent(PluginDef::class.java)) {
                    if (!plugin.javaClass.getAnnotation(PluginDef::class.java).support) {
                        putNode(plugin.toMeta())
                    }

                }
            }
        }
    }

    /**
     * Lock this context by given object
     *
     * @param obj
     */
    fun lock(obj: Any) {
        this.lock.lock(obj)
        parent?.lock(obj)
    }

    /**
     * Unlock the context by given object
     *
     * @param obj
     */
    fun unlock(obj: Any) {
        this.lock.unlock(obj)
        parent?.unlock(obj)
    }


    /**
     * Free up resources associated with this context
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun close() {
        //detach all plugins
        pluginManager.close()

        if (started) {
            dispatcher.shutdown()
        }
    }

    companion object {

        /**
         * Build a new context based on given meta
         *
         * @param name
         * @param parent
         * @param meta
         * @return
         */
        @JvmOverloads
        fun build(name: String, parent: Context = Global, meta: Meta = Meta.empty()): Context {
            val builder = ContextBuilder(name, parent)

            meta.useMeta("properties") { builder.properties(it) }

            meta.optString("rootDir").ifPresent { builder.setRootDir(it) }

            meta.optValue("classpath").ifPresent { value -> value.listValue().stream().map<String> { it.stringValue() }.forEach { builder.classPath(it) } }

            meta.getMetaList("plugin").forEach { builder.plugin(it) }

            return builder.build()
        }
    }

}

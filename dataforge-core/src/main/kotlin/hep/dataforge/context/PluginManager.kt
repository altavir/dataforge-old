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

import hep.dataforge.exceptions.ContextLockException
import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * The manager for plugin system. Should monitor plugin dependencies and locks.
 *
 * @property context A context for this plugin manager
 * @author Alexander Nozik
 */
class PluginManager(private val context: Context) : ContextAware, AutoCloseable {

    /**
     * A set of loaded plugins
     */
    private val plugins = HashSet<Plugin>()

    /**
     * A class path resolver
     */
    var pluginLoader: PluginLoader = ClassPathPluginLoader(context)

    private val parent: PluginManager? = context.parent?.pluginManager

    override fun getContext(): Context {
        return this.context
    }

    fun stream(recursive: Boolean): Stream<Plugin> {
        return if (recursive && parent != null) {
            Stream.concat(plugins.stream(), parent.stream(true))
        } else {
            plugins.stream()
        }
    }

    /**
     * Get for existing plugin
     */
    fun get(recursive: Boolean = true, predicate: (Plugin) -> Boolean): Plugin? {
        val plugins = stream(recursive).filter(predicate).toList()
        return when (plugins.size) {
            0 -> null
            1 -> plugins[0]
            else -> throw RuntimeException("Multiple candidates for plugin resolution")
        }
    }

    /**
     * Find a loaded plugin via its tag
     *
     * @param tag
     * @return
     */
    fun get(tag: PluginTag): Plugin? {
        return get(true) { tag.matches(it.tag) }
    }

    /**
     * Find a loaded plugin via its class
     *
     * @param tag
     * @param type
     * @param <T>
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Plugin> get(type: KClass<T>): T? {
        return get(true) { type.isInstance(it) } as T?
    }

    /**
     * Load given plugin into this manager and return loaded instance.
     * Throw error if plugin of the same class already exists in manager
     *
     * @param plugin
     * @return
     */
    fun <T : Plugin> load(plugin: T): T {
        if (context.isLocked) {
            throw ContextLockException()
        }

        if (get(plugin::class) != null) {
            throw  RuntimeException("Plugin of type ${plugin::class} already exists in ${context.name}")
        } else {
            for (tag in plugin.dependsOn()) {
                load(tag)
            }

            logger.info("Loading plugin {} into {}", plugin.name, context.name)
            plugin.attach(getContext())
            plugins.add(plugin)
            return plugin
        }
    }

    /**
     * Get plugin instance via plugin reolver and load it.
     *
     * @param tag
     * @return
     */
    @JvmOverloads
    fun load(tag: PluginTag, meta: Meta = Meta.empty()): Plugin {
        val loaded = get(tag)
        return when {
            loaded == null -> load(pluginLoader[tag, meta])
            loaded.meta == meta -> loaded // if meta is the same, return existing plugin
            else -> throw RuntimeException("Can't load plugin with tag $tag. Plugin with this tag and different configuration already exists in context.")
        }
    }

    /**
     * Load plugin by its class and meta. Ignore if plugin with this meta is already loaded.
     */
    @JvmOverloads
    fun <T : Plugin> load(type: KClass<T>, meta: Meta = Meta.empty()): T {
        val loaded = get(type)
        return when {
            loaded == null -> load(pluginLoader[type, meta])
            loaded.meta == meta -> loaded // if meta is the same, return existing plugin
            else -> throw RuntimeException("Can't load plugin with type $type. Plugin with this type and different configuration already exists in context.")
        }
    }

    @JvmOverloads
    fun <T : Plugin> load(type: Class<T>, meta: Meta = Meta.empty()): T {
        return load(type.kotlin, meta)
    }

    @JvmOverloads
    fun load(name: String, meta: Meta = Meta.empty()): Plugin {
        return load(PluginTag.fromString(name), meta)
    }

    @Throws(Exception::class)
    override fun close() {
        this.plugins.forEach { it.detach() }
    }

    /**
     * List loaded plugins
     *
     * @return
     */
    fun list(): Collection<Plugin> {
        return this.plugins
    }
}

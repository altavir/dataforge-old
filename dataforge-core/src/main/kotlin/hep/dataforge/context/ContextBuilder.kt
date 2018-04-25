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

import hep.dataforge.io.DefaultIOManager
import hep.dataforge.io.IOManager
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaUtils
import hep.dataforge.values.Value
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.function.BiPredicate
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A builder for context
 */
class ContextBuilder(val name: String, val parent: Context = Global) {

    val properties = HashMap<String, Value>()

    private val classPath = ArrayList<URL>()

    private val plugins = ArrayList<Plugin>()


    var rootDir: String
        get() = properties[IOManager.ROOT_DIRECTORY_CONTEXT_KEY]?.toString() ?: parent.io.rootDir.toString()
        set(value) {
            setDefaultIO()
            val path = parent.io.rootDir.resolve(value)
            //Add libraries to classpath
            val libPath = path.resolve("lib")
            if (Files.isDirectory(libPath)) {
                classPath(libPath.toUri())
            }
            properties[IOManager.ROOT_DIRECTORY_CONTEXT_KEY] = Value.of(path.toString())
        }

    var dataDir: String
        get() = properties[IOManager.DATA_DIRECTORY_CONTEXT_KEY]?.toString()
                ?: parent.getString(IOManager.DATA_DIRECTORY_CONTEXT_KEY, parent.io.rootDir.toString())
        set(value) {
            setDefaultIO()
            properties[IOManager.DATA_DIRECTORY_CONTEXT_KEY] = Value.of(value)
        }

    fun properties(config: Meta): ContextBuilder {
        if (config.hasMeta("property")) {
            config.getMetaList("property").forEach { propertyNode ->
                properties[propertyNode.getString("key")] = propertyNode.getValue("value")
            }
        } else if (config.name == "properties") {
            MetaUtils.valueStream(config).forEach { pair -> properties[pair.first] = pair.second }
        }
        return this
    }

    fun plugin(plugin: Plugin): ContextBuilder {
        this.plugins.add(plugin)
        return this
    }

    /**
     * Set default IO if another IO not already defined
     */
    fun setDefaultIO(): ContextBuilder {
        return if (plugins.none { it is IOManager }) {
            plugin(DefaultIOManager())
        } else {
            this
        }
    }

    /**
     * Load and configure a plugin. Use parent PluginLoader for resolution
     *
     * @param type
     * @param meta
     * @return
     */
    @JvmOverloads
    fun plugin(type: Class<out Plugin>, meta: Meta = Meta.empty()): ContextBuilder {
        val tag = PluginTag.resolve(type)
        return plugin(parent.pluginManager.pluginLoader.get(tag, meta))
    }

    fun plugin(tag: String, meta: Meta): ContextBuilder {
        val pluginTag = PluginTag.fromString(tag)
        return plugin(parent.pluginManager.pluginLoader.get(pluginTag, meta))
    }


    @Suppress("UNCHECKED_CAST")
    fun plugin(meta: Meta): ContextBuilder {
        val plMeta = meta.getMetaOrEmpty(MetaBuilder.DEFAULT_META_NAME)
        return when {
            meta.hasValue("name") -> plugin(meta.getString("name"), plMeta)
            meta.hasValue("class") -> {
                val type: Class<out Plugin> = Class.forName(meta.getString("class")) as? Class<out Plugin>
                        ?: throw RuntimeException("Failed to initialize plugin from meta")
                plugin(type, plMeta)
            }
            else -> throw IllegalArgumentException("Malformed plugin definition")
        }
    }

    fun classPath(vararg path: URL): ContextBuilder {
        classPath.addAll(Arrays.asList(*path))
        return this
    }

    fun classPath(path: URI): ContextBuilder {
        try {
            classPath.add(path.toURL())
        } catch (e: MalformedURLException) {
            throw RuntimeException("Malformed classpath")
        }

        return this
    }

    /**
     * Create additional classpath from a list of strings
     *
     * @param pathStr
     * @return
     */
    fun classPath(pathStr: String): ContextBuilder {
        val path = Paths.get(pathStr)
        return when {
            Files.isDirectory(path) -> try {
                Files.find(path, -1, BiPredicate { subPath, _ -> subPath.toString().endsWith(".jar") })
                        .map<URI> { it.toUri() }.forEach { this.classPath(it) }
                this
            } catch (e: IOException) {
                throw RuntimeException("Failed to load library", e)
            }
            Files.exists(path) -> classPath(path.toUri())
            else -> this
        }
    }

    fun classPath(paths: Collection<URL>): ContextBuilder {
        classPath.addAll(paths)
        return this
    }

    /**
     * Create new IO manager for this context if needed (using default input and output of parent) and set its root
     *
     * @param rootDir
     * @return
     */
    fun setRootDir(rootDir: String): ContextBuilder {
        this.rootDir = rootDir
        return this
    }

    fun setDataDir(dataDir: String): ContextBuilder {
        this.rootDir = dataDir
        return this
    }

    fun build(): Context {
        // automatically add lib directory
        val classLoader = if (classPath.isEmpty()) {
            null
        } else {
            URLClassLoader(classPath.toTypedArray(), parent.classLoader)
        }
        return Context(name, parent, classLoader).apply {
            plugins.forEach {
                pluginManager.load(it)
            }
        }

    }
}

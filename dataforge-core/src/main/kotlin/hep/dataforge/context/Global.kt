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

import hep.dataforge.kodex.orElse
import hep.dataforge.utils.ReferenceRegistry
import hep.dataforge.values.Value
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A singleton global context. Automatic root for the whole context hierarchy. Also stores the registry for active contexts.
 *
 * @author Alexander Nozik
 */
object Global : Context("GLOBAL", null, Thread.currentThread().contextClassLoader) {

    init {
        Locale.setDefault(Locale.US)
    }

    /**
     * The global context independent temporary user directory. This directory
     * is used to store user configuration files. Never use it to store data.
     *
     * @return
     */
    val userDirectory: File
        get() {
            val userDir = File(System.getProperty("user.home"))
            val dfUserDir = File(userDir, ".dataforge")
            if (!dfUserDir.exists()) {
                dfUserDir.mkdir()
            }
            return dfUserDir
        }

    override val history: Chronicler by lazy { Chronicler().apply { startGlobal() } }

    override val io: IOManager
        get() = pluginManager.get(IOManager::class).orElse {
            logger.debug("No IO plugin found. Using default IO.")
            pluginManager.load(DefaultIOManager())
        }

    override val executor: ExecutorPlugin
        get() = pluginManager.get(ExecutorPlugin::class).orElse {
            logger.debug("No executor plugin found. Using default executor.")
            pluginManager.load(DefaultExecutorPlugin())
        }

    /**
     * {@inheritDoc}
     *
     * @param path
     * @return
     */
    override fun optValue(path: String): Optional<Value> {
        return Optional.ofNullable(getProperties()[path])
    }

    /**
     * {@inheritDoc}
     *
     * @param path
     * @return
     */
    override fun hasValue(path: String): Boolean {
        return getProperties().containsKey(path)
    }

    /**
     * Closing all contexts
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun close() {
        logger.info("Shutting down GLOBAL")
        for (ctx in contextRegistry) {
            ctx.close()
        }
        dispatchThreadExecutor.shutdown()
        super.close()
    }


    private val contextRegistry = ReferenceRegistry<Context>()
    private val dispatchThreadExecutor = Executors.newSingleThreadExecutor { r ->
        val res = Thread(r, "DF_DISPATCH")
        res.priority = Thread.MAX_PRIORITY
        res
    }

    /**
     * Use that context when the context for some reason is not provided. By default throws a runtime exception.
     *
     * @return
     */
    @JvmStatic
    var defaultContext: Context? = null
        get() {
            if (field == null) {
                throw RuntimeException("Context not specified")
            }
            return field
        }

    /**
     * A single thread executor for DataForge messages dispatch. No heavy calculations should be done on this thread
     *
     * @return
     */
    fun dispatchThreadExecutor(): ExecutorService {
        return dispatchThreadExecutor
    }

    /**
     * Get previously builder context o builder a new one
     *
     * @param name
     * @return
     */
    @Synchronized
    fun getContext(name: String): Context {
        return contextRegistry
                .findFirst { ctx -> ctx.name == name }
                .orElseGet {
                    val ctx = Context(name)
                    contextRegistry.add(ctx)
                    ctx
                }
    }

    /**
     * Close all contexts and terminate framework
     */
    fun terminate() {
        try {
            close()
        } catch (e: Exception) {
            logger.error("Exception while terminating DataForge framework", e)
        }

    }


}

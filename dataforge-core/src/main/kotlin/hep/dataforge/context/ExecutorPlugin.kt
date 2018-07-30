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

import hep.dataforge.meta.Meta
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.cancel
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Plugin managing execution
 */
interface ExecutorPlugin : Plugin {
    /**
     * Default executor for this plugin
     */
    val defaultExecutor: ExecutorService

    /**
     * Create or load custom executor
     */
    fun getExecutor(meta: Meta): ExecutorService

    /**
     * A dispatcher for default coroutine thread pool
     */
    val kDispatcher: CoroutineContext
}

@PluginDef(group = "hep.dataforge", name = "executor", support = true, info = "Executor plugin")
class DefaultExecutorPlugin(meta: Meta = Meta.empty()) : BasicPlugin(meta), ExecutorPlugin {
    private val executors = HashMap<Meta, ExecutorService>();

    /**
     * Create a default executor that uses plugin meta
     */
    override val defaultExecutor: ExecutorService by lazy {
        logger.info("Initializing default executor in {}", context.name)
        getExecutor(meta)
    }

    override fun getExecutor(meta: Meta): ExecutorService {
        synchronized(context) {
            return executors.getOrPut(meta) {
                val workerName = meta.getString("workerName", "worker");
                val threads = meta.getInt("threads", Runtime.getRuntime().availableProcessors())
                val factory = { pool: ForkJoinPool ->
                    ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool).apply {
                        name = "${context.name}_$workerName-$poolIndex"
                    }
                }
                ForkJoinPool(
                        threads,
                        factory, null, false)
            }
        }
    }

    override val kDispatcher by lazy { defaultExecutor.asCoroutineDispatcher() }


    override fun detach() {
        executors.values.forEach { it.shutdown() }
        kDispatcher.cancel(CancellationException("Executor detached"))
        super.detach()
    }
}


/*
 * Copyright  2017 Alexander Nozik.
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
package hep.dataforge.control.ports

import hep.dataforge.exceptions.PortException
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import java.util.function.Supplier

/**
 * @author Alexander Nozik
 */
abstract class VirtualPort protected constructor(meta: Meta) : Port(meta), Configurable {

    private val futures = CopyOnWriteArraySet<TaggedFuture>()
    private var scheduler: ScheduledExecutorService? = null
    override var isOpen = false
    private var configuration = Configuration("virtualPort")

    init {
        configuration = Configuration(meta)
    }

    @Throws(PortException::class)
    override fun open() {
        scheduler = Executors.newScheduledThreadPool(meta.getInt("numThreads", 4)!!)
        isOpen = true
    }

    override fun getConfig(): Configuration {
        return configuration
    }

    override fun configure(config: Meta): Configurable {
        configuration.update(config)
        return this
    }

    override fun getMeta(): Meta {
        return configuration
    }

    override fun toString(): String {
        return meta.getString("id", javaClass.simpleName)
    }

    @Throws(PortException::class)
    public override fun send(message: String) {
        evaluateRequest(message)
    }

    /**
     * The device logic here
     *
     * @param request
     */
    protected abstract fun evaluateRequest(request: String)

    @Synchronized protected fun clearCompleted() {
        futures.stream().filter { future -> future.future.isDone || future.future.isCancelled }.forEach{ futures.remove(it) }
    }

    @Synchronized protected fun cancelByTag(tag: String) {
        futures.stream().filter { future -> future.hasTag(tag) }.forEach{ it.cancel() }
    }

    /**
     * Plan the response with given delay
     *
     * @param response
     * @param delay
     * @param tags
     */
    @Synchronized protected fun planResponse(response: String, delay: Duration, vararg tags: String) {
        clearCompleted()
        val task = { receivePhrase(response) }
        val future = scheduler!!.schedule(task, delay.toNanos(), TimeUnit.NANOSECONDS)
        this.futures.add(TaggedFuture(future, *tags))
    }

    @Synchronized protected fun planRegularResponse(responseBuilder: Supplier<String>, delay: Duration, period: Duration, vararg tags: String) {
        clearCompleted()
        val task = { receivePhrase(responseBuilder.get()) }
        val future = scheduler!!.scheduleAtFixedRate(task, delay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS)
        this.futures.add(TaggedFuture(future, *tags))
    }

    @Throws(Exception::class)
    override fun close() {
        futures.clear()
        this.scheduler!!.shutdownNow()
        isOpen = false
        super.close()
    }

    private inner class TaggedFuture(internal var future: ScheduledFuture<*>, vararg tags: String) {
        internal var tags: MutableSet<String>

        init {
            this.tags = HashSet()
            this.tags.addAll(Arrays.asList(*tags))
        }

        fun hasTag(tag: String): Boolean {
            return tags.contains(tag)
        }

        fun cancel(): Boolean {
            return future.cancel(true)
        }
    }
}

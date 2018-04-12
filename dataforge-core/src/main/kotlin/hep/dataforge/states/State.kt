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

package hep.dataforge.states

import hep.dataforge.Named
import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDescriptor
import hep.dataforge.kodex.buildMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaID
import hep.dataforge.meta.MetaMorph
import hep.dataforge.meta.morph
import hep.dataforge.values.Value
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.time.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A logical state possibly backed by physical state
 */
sealed class State<T : Any>(
        final override val name: String,
        def: T? = null,
        buffer: Int = 100,
        private val getter: (suspend () -> T)? = null,
        private val setter: (suspend (T?, T) -> Unit)? = null) : Named, MetaID {
    private var valid: Boolean = false

    //TODO do something with logging names
    var logger: Logger = LoggerFactory.getLogger("state::$name")

    private val parentJob = Job()
    private val ref = AtomicReference<T>()
    val channel = BroadcastChannel<T>(buffer)

    /**
     * Open subscription for updates of this state
     */
    fun subscribe(): SubscriptionReceiveChannel<T> {
        return channel.openSubscription()
    }

    fun onChange(action: suspend (T) -> Unit) {
        val subscription = subscribe()
        launch(parent = parentJob) {
            while (true) {
                action(subscription.receive())
            }
        }
    }

    init {
        if (def != null) {
            channel.offer(def)
            ref.set(def)
            valid = true
        }
    }

    /**
     * Update the logical value without triggering the change of backing physical state
     */
    private fun updateValue(value: T) {
        ref.set(value)
        channel.offer(value)
        valid = true
        logger.debug("State {} changed to {}", name, value)
    }

    protected abstract fun transform(value: Any): T

    /**
     * Update state with any object automatically casting it to required type or throwing exception
     */
    fun update(value: Any?) {
        if (value == null) {
            invalidate()
        } else {
            updateValue(transform(value))
        }
    }

    /**
     * If setter is provided, launch it asynchronously without changing logical state.
     * If the setter produces non-null result, it is asynchronously updated logical value.
     * Otherwise just change the logical state.
     */
    fun set(value: Any?) {
        if (value == null) {
            invalidate()
        } else {
            val transformed = transform(value)
            setter?.let {
                launch {
                    it.invoke(ref.get(), transformed)
                }
            } ?: update(value)
        }
    }

    /**
     * Set the value and block calling thread until it is set or until timeout expires
     */
    fun setValueAndWait(value: T, timeout: Duration? = null): T {
        val deferred = setter?.let {
            async<T> {
                it.invoke(ref.get(), value)
                return@async channel.openSubscription().receive()
            }
        } ?: async {
            update(value)
            return@async value
        }
        return runBlocking {
            if (timeout == null) {
                deferred.await()
            } else {
                withTimeout(timeout) { deferred.await() }
            }
        }
    }

    fun setAndWait(value: Any?, timeout: Duration? = null): T {
        if (value == null) {
            invalidate()
            return runBlocking { read(timeout) }
        }
        return setValueAndWait(transform(value), timeout)
    }

    /**
     * Get current value or invoke getter if it is present. Getter is invoked in blocking mode. If state is invalid
     */
    private fun get(): T {
        return if (valid) {
            ref.get()
        } else {
            runBlocking { read() }
        }
    }

    /**
     * Invalidate current state value and force it to be re-aquired from physical state on next call.
     * If getter is not defined, then subsequent calls will produce error.
     */
    fun invalidate() {
        valid = false
    }

    /**
     * read the state if the getter is available and update logical
     */
    suspend fun read(): T {
        if (getter == null) {
            throw RuntimeException("The getter for state $name not defined")
        } else {
            val res = getter.invoke()
            updateValue(res)
            return res
        }
    }

    suspend fun read(timeout: Duration?): T {
        return if (timeout == null) {
            read()
        } else {
            withTimeout(timeout) {
                read()
            }
        }
    }

    fun readBlocking(): T {
        return runBlocking {
            read()
        }
    }

    /**
     * Read == get()
     * Write == set()
     */
    var value: T
        get() = get()
        set(value) = set(value)

    val delegate = object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this@State.value = value
        }
    }
}

private fun (suspend () -> Any).toValue(): (suspend () -> Value) {
    return { Value.of(this.invoke()) }
}

//private fun (suspend (Value?, Value) -> Any?).toValue(): (suspend (Value?, Value) -> Value?) {
//    return { old, new -> Value.of(this.invoke(old, new)) }
//}


class ValueState(
        name: String,
        val descriptor: ValueDescriptor = ValueDescriptor.empty(name),
        def: Any? = null,
        getter: (suspend () -> Any)? = null,
        setter: (suspend (Value?, Value) -> Unit)? = null
) : State<Value>(name, def?.let { Value.of(it) }, getter = getter?.toValue(), setter = setter) {

    constructor(
            def: ValueDef,
            getter: (suspend () -> Any)? = null,
            setter: (suspend (Value?, Value) -> Unit)? = null
    ) : this(def.name, ValueDescriptor.build(def), Value.of(def.def), getter, setter)

    override fun transform(value: Any): Value {
        return Value.of(value)
    }

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name, "value" to value)
    }

    val booleanDelegate: ReadWriteProperty<Any?, Boolean> = object : ReadWriteProperty<Any?, Boolean> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            set(value)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return value.booleanValue()
        }
    }

    val booleanValue
        get() = value.booleanValue()

    val stringDelegate: ReadWriteProperty<Any?, String> = object : ReadWriteProperty<Any?, String> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            set(value)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return value.stringValue()
        }
    }

    val stringValue: String
        get() = value.stringValue()

    val timeDelegate: ReadWriteProperty<Any?, Instant> = object : ReadWriteProperty<Any?, Instant> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Instant) {
            set(value)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Instant {
            return value.timeValue()
        }
    }

    val timeValue: Instant
        get() = value.timeValue()

    val intDelegate: ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            set(value)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return value.intValue()
        }
    }

    val intValue
        get() = value.intValue()

    val doubleDelegate: ReadWriteProperty<Any?, Double> = object : ReadWriteProperty<Any?, Double> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
            set(value)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
            return value.doubleValue()
        }
    }

    val doubleValue
        get() = value.doubleValue()

    inline fun <reified T : Enum<T>> enumDelegate(): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return enumValueOf<T>(value.stringValue())
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value.name)
        }
    }

    inline fun <reified T : Enum<T>> enumValue(): T {
        return enumValueOf(value.stringValue())
    }

}

fun ValueState(def: StateDef): ValueState {
    return ValueState(def.value)
}


class MetaState(
        name: String,
        val descriptor: NodeDescriptor = NodeDescriptor.empty(name),
        def: Meta? = null,
        getter: (suspend () -> Meta)? = null,
        setter: (suspend (Meta?, Meta) -> Unit)? = null
) : State<Meta>(name, def, getter = getter, setter = setter) {
    constructor(
            def: NodeDef,
            getter: (suspend () -> Meta)? = null,
            setter: (suspend (Meta?, Meta) -> Unit)? = null
    ) : this(def.name, NodeDescriptor.build(def), null, getter, setter)// TODO fix default value

    override fun transform(value: Any): Meta {
        return (value as? MetaID)?.toMeta()
                ?: throw RuntimeException("The state $name requires meta-convertible value, but found ${value::class}")
    }

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name) {
            putNode("value", value)
        }
    }
}

fun MetaState(def: MetaStateDef): MetaState {
    return MetaState(def.value)
}

class MorphState<T : MetaMorph>(
        name: String,
        val type: KClass<T>,
        def: T? = null,
        getter: (suspend () -> T)? = null,
        setter: (suspend (T?, T) -> Unit)? = null
) : State<T>(name, def, getter = getter, setter = setter) {
    override fun transform(value: Any): T {
        return (value as? MetaMorph)?.morph(type)
                ?: throw RuntimeException("The state $name requires metamorph value, but found ${value::class}")
    }

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name) {
            putNode("value", value)
        }
    }
}

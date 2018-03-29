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
import hep.dataforge.values.Value
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.atomic.AtomicReference

/**
 * A logical state possibly backed by physical state
 */
sealed class State<T : Any>(
        override val name: String, def: T? = null, buffer: Int = 0,
        private val getter: (suspend () -> T)? = null,
        private val setter: (suspend (T) -> T?)? = null) : Named, MetaID {
    private var initialized: Boolean = false
    private val reference: AtomicReference<T> = AtomicReference()
    private val _channel: Channel<T> = Channel(buffer)

    /**
     * An unbuffered read-only channel for values of the state
     */
    val channel: ReceiveChannel<T> = _channel

    init {
        if (def != null) {
            reference.set(def)
            initialized = true
        }
    }

    /**
     * Update the logical value without triggering the change of backing physical state
     */
    fun update(value: T) {
        initialized = true
        reference.set(value)
        async {
            _channel.send(value)
        }
    }

    /**
     * If setter is provided, launch it asynchronously without changing logical state.
     * If the setter produces non-null result, it is asynchronously updated logical value.
     * Otherwise just change the logical state.
     */
    open fun set(value: T) {
        setter?.let {
            async {
                val res = it(value)
                if (res != null) {
                    update(res)
                }
            }
        } ?: update(value)
    }

    /**
     * Get current value or invoke getter if it is present. Getter is invoked in blocking mode. If state is invalid
     */
    fun get(): T {
        return if (initialized) {
            reference.get()
        } else {
            if (getter == null) {
                throw RuntimeException("The state $name not initialized")
            } else {
                runBlocking { getter.invoke() }.also { update(it) }
            }
        }
    }

    /**
     * Invalidate current state value and force it to be re-aquired from physical state on next call.
     * If getter is not defined, then subsequent calls will produce error.
     */
    fun invalidate() {
        initialized = false
    }


    /**
     * Read == get()
     * Write == set()
     */
    var value: T
        get() = get()
        set(value) = set(value)

}

private fun (suspend () -> Any).toValue(): (suspend () -> Value) {
    return { Value.of(this.invoke()) }
}

private fun (suspend (Value) -> Any?).toValue(): (suspend (Value) -> Value?) {
    return { Value.of(this.invoke(it)) }
}


class ValueState(
        name: String,
        val descriptor: ValueDescriptor = ValueDescriptor.empty(name),
        def: Any? = null,
        buffer: Int = 0,
        getter: (suspend () -> Any)? = null,
        setter: (suspend (Value) -> Any?)? = null
) : State<Value>(name, def?.let { Value.of(it) }, buffer, getter?.toValue(), setter?.toValue()) {

    constructor(
            def: ValueDef,
            buffer: Int = 0,
            getter: (suspend () -> Any)? = null,
            setter: (suspend (Value) -> Any?)? = null
    ) : this(def.name, ValueDescriptor.build(def), Value.of(def.def), buffer, getter, setter)

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name, "value" to value)
    }
}

fun ValueState(def: StateDef):ValueState{
    return ValueState(def.value)
}


class MetaState<T : MetaID>(
        name: String,
        val descriptor: NodeDescriptor,
        def: T? = null,
        buffer: Int = 0,
        getter: (suspend () -> T)? = null,
        setter: (suspend (T) -> T?)? = null
) : State<T>(name, def, buffer, getter, setter) {

    constructor(
            def: NodeDef,
            buffer: Int = 0,
            getter: (suspend () -> T)? = null,
            setter: (suspend (T) -> T?)? = null
    ) : this(def.name, NodeDescriptor.build(def), null, buffer, getter, setter)// TODO fix default value

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name) {
            putNode("value", value)
        }
    }
}

fun MetaState(def: MetaStateDef): MetaState<*>{
    return MetaState<MetaID>(def.value)
}

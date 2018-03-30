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
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

/**
 * A logical state possibly backed by physical state
 */
sealed class State<T : Any>(
        override val name: String, def: T? = null,
        private val getter: (suspend () -> T)? = null,
        private val setter: (suspend (T) -> T?)? = null) : Named, MetaID {
    private var initialized: Boolean = false
    private val reference: AtomicReference<T> = AtomicReference()

    private var _future = CompletableDeferred<T>()
    /**
     * The future representing next state change. It is reset with the new reference after each complete
     */
    val future: Deferred<T>
        get() = _future

    init {
        if (def != null) {
            reference.set(def)
            initialized = true
        }
    }

    /**
     * Update the logical value without triggering the change of backing physical state
     */
    fun updateValue(value: T) {
        initialized = true
        reference.set(value)
        //Complete and resed the future
        _future.complete(value)
        _future = CompletableDeferred<T>()
    }

    /**
     * Update state with any object automatically casting it to required type or throwing exception
     */
    abstract fun update(value: Any)

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
                    updateValue(res)
                }
            }
        } ?: updateValue(value)
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
                runBlocking { getter.invoke() }.also { updateValue(it) }
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
        getter: (suspend () -> Any)? = null,
        setter: (suspend (Value) -> Any?)? = null
) : State<Value>(name, def?.let { Value.of(it) }, getter?.toValue(), setter?.toValue()) {

    constructor(
            def: ValueDef,
            getter: (suspend () -> Any)? = null,
            setter: (suspend (Value) -> Any?)? = null
    ) : this(def.name, ValueDescriptor.build(def), Value.of(def.def), getter, setter)

    override fun update(value: Any) {
        updateValue(Value.of(value))
    }

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name, "value" to value)
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
        setter: (suspend (Meta) -> Meta?)? = null
) : State<Meta>(name, def, getter, setter) {

    constructor(
            def: NodeDef,
            getter: (suspend () -> Meta)? = null,
            setter: (suspend (Meta) -> Meta?)? = null
    ) : this(def.name, NodeDescriptor.build(def), null, getter, setter)// TODO fix default value

    override fun update(value: Any) {
        val metaValue = value as? MetaID
                ?: throw RuntimeException("The state $name requires meta-convertible value, but found ${value::class}")
        updateValue(metaValue.toMeta())
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
        setter: (suspend (T) -> T?)? = null
) : State<T>(name, def, getter, setter) {

    override fun update(value: Any) {
        if (value is MetaMorph) {
            updateValue(value.morph(type))
        }
    }

    override fun toMeta(): Meta {
        return buildMeta("state", "name" to name) {
            putNode("value", value)
        }
    }
}

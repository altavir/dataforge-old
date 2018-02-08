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

package hep.dataforge.kodex

import hep.dataforge.description.Described
import hep.dataforge.description.Descriptors
import hep.dataforge.description.PropertyDef
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaMorph
import hep.dataforge.meta.MutableMetaNode
import hep.dataforge.meta.SealedNode
import hep.dataforge.values.Value
import java.time.Instant
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

//Metoid values

private fun getDescribedValue(valueName: String, thisRef: Any, property: KProperty<*>): Value {
    val propertyAnnotation = property.findAnnotation<PropertyDef>()
    return when {
        propertyAnnotation != null -> Value.of(propertyAnnotation.def)
        else -> Descriptors.extractValue(valueName, Descriptors.buildDescriptor(thisRef))
    }
}

private fun getDescribedNode(nodeName: String, thisRef: Any, property: KProperty<*>): Meta {
    return if (thisRef is Described) thisRef.descriptor.optChildDescriptor(nodeName).map {
        if (it.hasDefault()) {
            it.defaultNode().first()
        } else {
            Meta.empty()
        }
    }.orElse(Meta.empty())
    else Meta.empty()
}


/**
 * The delegate for value inside meta
 * Values for sealed meta are automatically cached
 * @property valueName name of the property. If null, use property name
 */
class ValueDelegate<out T>(
        val target: Meta,
        val valueName: String? = null,
        val def: T? = null,
        private val converter: (Value) -> T
) : ReadOnlyProperty<Any, T> {

    private var cached: T? = null

    private fun getValueInternal(thisRef: Any, property: KProperty<*>): T {
        val key = valueName ?: property.name
        return when {
            target.hasValue(key) -> converter(target.getValue(key))
            def != null -> def
            else -> converter(getDescribedValue(key, thisRef, property))
        }
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (target is SealedNode && cached == null) {
            cached = getValueInternal(thisRef, property)
        }
        return cached ?: getValueInternal(thisRef, property)
    }
}

class NodeDelegate<out T>(
        val target: Meta,
        private val nodeName: String?,
        val def: T? = null,
        private val converter: (Meta) -> T
) : ReadOnlyProperty<Any, T> {

    private var cached: T? = null

    private fun getNodeInternal(thisRef: Any, property: KProperty<*>): T {
        val key = nodeName ?: property.name
        return when {
            target.hasMeta(key) -> converter(target.getMeta(key))
            def != null -> def
            else -> converter(getDescribedNode(key, thisRef, property))
        }
    }


    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (target is SealedNode && cached == null) {
            cached = getNodeInternal(thisRef, property)
        }
        return cached ?: getNodeInternal(thisRef, property)
    }
}

/*
 * Delegate value and meta getter to target meta using thisref description
 */

fun Meta.value(valueName: String? = null, def: Value? = null): ReadOnlyProperty<Any, Value> =
        ValueDelegate(this, valueName, def) { it }

fun Meta.stringValue(valueName: String? = null, def: String? = null): ReadOnlyProperty<Any, String> =
        ValueDelegate(this, valueName, def) { it.stringValue() }

fun Meta.booleanValue(valueName: String? = null, def: Boolean? = null): ReadOnlyProperty<Any, Boolean> =
        ValueDelegate(this, valueName, def) { it.booleanValue() }

fun Meta.timeValue(valueName: String? = null, def: Instant? = null): ReadOnlyProperty<Any, Instant> =
        ValueDelegate(this, valueName, def) { it.timeValue() }

fun Meta.numberValue(valueName: String? = null, def: Number? = null): ReadOnlyProperty<Any, Number> =
        ValueDelegate(this, valueName, def) { it.numberValue() }

fun Meta.doubleValue(valueName: String? = null, def: Double? = null): ReadOnlyProperty<Any, Double> =
        ValueDelegate(this, valueName, def) { it.doubleValue() }

fun Meta.intValue(valueName: String? = null, def: Int? = null): ReadOnlyProperty<Any, Int> =
        ValueDelegate(this, valueName, def) { it.intValue() }

fun <T> Meta.customValue(valueName: String? = null, def: T? = null, conv: (Value) -> T): ReadOnlyProperty<Any, T> =
        ValueDelegate(this, valueName, def, conv)

fun Meta.node(nodeName: String? = null, def: Meta? = null): ReadOnlyProperty<Any, Meta> =
        NodeDelegate(this, nodeName, def) { it }

fun <T> Meta.customNode(nodeName: String? = null, def: T? = null, conv: (Meta) -> T): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def, conv)

fun <T : MetaMorph> Meta.morphNode(type: KClass<T>, nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def) { MetaMorph.morph(type, it) }

inline fun <reified T : MetaMorph> Meta.morphNode(nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        morphNode(T::class, nodeName, def)


//Configuration extension

class MutableValueDelegate<T>(
        val target: MutableMetaNode<*>,
        private val valueName: String? = null,
        val def: T? = null,
        private val converter: (Value) -> T,
        private val toValue: (T) -> Any) : ReadWriteProperty<Any, T> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        val key = valueName ?: property.name
        return when {
            target.hasValue(key) -> converter(target.getValue(key))
            def != null -> def
            else -> converter(getDescribedValue(key, thisRef, property))
        }
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        //TODO set for allowed values
        target.setValue(valueName ?: property.name, value)
    }
}

class MutableMetaDelegate<T>(
        val target: MutableMetaNode<*>,
        private val nodeName: String?,
        val def: T? = null,
        private val converter: (Meta) -> T,
        val toMeta: (T) -> Meta) : ReadWriteProperty<Any, T> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        val key = nodeName ?: property.name
        return when {
            target.hasMeta(key) -> converter(target.getMeta(key))
            def != null -> def
            else -> converter(getDescribedNode(key, thisRef, property))
        }
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        target.setNode(nodeName ?: property.name, toMeta(value))
    }
}

fun MutableMetaNode<*>.mutableValue(valueName: String? = null, def: Value? = null): ReadWriteProperty<Any, Value> =
        MutableValueDelegate(this, valueName, def, { it }, { it })

fun MutableMetaNode<*>.mutableStringValue(valueName: String? = null, def: String? = null): ReadWriteProperty<Any, String> =
        MutableValueDelegate(this, valueName, def, { it.stringValue() }, { Value.of(it) })

fun MutableMetaNode<*>.mutableBooleanValue(valueName: String? = null, def: Boolean? = null): ReadWriteProperty<Any, Boolean> =
        MutableValueDelegate(this, valueName, def, { it.booleanValue() }, { Value.of(it) })

fun MutableMetaNode<*>.mutableTimeValue(valueName: String? = null, def: Instant? = null): ReadWriteProperty<Any, Instant> =
        MutableValueDelegate(this, valueName, def, { it.timeValue() }, { Value.of(it) })

fun MutableMetaNode<*>.mutableNumberValue(valueName: String? = null, def: Number? = null): ReadWriteProperty<Any, Number> =
        MutableValueDelegate(this, valueName, def, { it.numberValue() }, { Value.of(it) })

fun MutableMetaNode<*>.mutableDoubleValue(valueName: String? = null, def: Double? = null): ReadWriteProperty<Any, Double> =
        MutableValueDelegate(this, valueName, def, { it.doubleValue() }, { Value.of(it) })

fun MutableMetaNode<*>.mutableIntValue(valueName: String? = null, def: Int? = null): ReadWriteProperty<Any, Int> =
        MutableValueDelegate(this, valueName, def, { it.intValue() }, { Value.of(it) })

fun <T> MutableMetaNode<*>.mutableCustomValue(valueName: String? = null, def: T? = null, read: (Value) -> T, write: (T) -> Any): ReadWriteProperty<Any, T> =
        MutableValueDelegate(this, valueName, def, read, write)

/**
 * Returns a child node of given meta that could be edited in-place
 */
inline fun <reified M : MutableMetaNode<out MutableMetaNode<*>>> MutableMetaNode<M>.mutableNode(metaName: String? = null, def: M? = null): ReadWriteProperty<Any, M> =
        MutableMetaDelegate<M>(this, metaName, def, { it as M }, { it })

fun <T> MutableMetaNode<*>.mutableCustomNode(metaName: String? = null, def: T? = null, read: (Meta) -> T, write: (T) -> Meta): ReadWriteProperty<Any, T> =
        MutableMetaDelegate(this, metaName, def, read, write)

/**
 * Create a property that is delegate for configurable
 */
fun <T : MetaMorph> MutableMetaNode<*>.mutableMorphNode(type: KClass<T>, metaName: String? = null, def: T? = null): ReadWriteProperty<Any, T> {
    return MutableMetaDelegate(this, metaName, def, { MetaMorph.morph(type, it) }, { it.toMeta() })
}

inline fun <reified T : MetaMorph> MutableMetaNode<*>.mutableMorphNode(metaName: String? = null, def: T? = null): ReadWriteProperty<Any, T> =
        mutableMorphNode(T::class, metaName, def)
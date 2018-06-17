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
import hep.dataforge.meta.*
import hep.dataforge.values.Value
import hep.dataforge.values.parseValue
import java.time.Instant
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

//Metoid values

private fun getDescribedValue(valueName: String, thisRef: Any?, property: KProperty<*>): Value {
    val propertyAnnotation = property.findAnnotation<PropertyDef>()
    return when {
        propertyAnnotation != null -> propertyAnnotation.def.parseValue()
        thisRef != null -> Descriptors.extractValue(valueName, Descriptors.buildDescriptor(thisRef))
        else -> Value.NULL
    }
}

private fun getDescribedNode(nodeName: String, thisRef: Any, property: KProperty<*>): Meta {
    return if (thisRef is Described) thisRef.descriptor.optChildDescriptor(nodeName).map {
        if (it.hasDefault()) {
            it.defaultNode()?.first() ?: Meta.empty()
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
) : ReadOnlyProperty<Any?, T> {

    private var cached: T? = null

    private fun getValueInternal(thisRef: Any?, property: KProperty<*>): T {
        val key = valueName ?: property.name
        return when {
            target.hasValue(key) -> converter(target.getValue(key))
            def != null -> def
            else -> converter(getDescribedValue(key, thisRef, property))
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
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

fun Meta.value(valueName: String? = null, def: Value? = null): ReadOnlyProperty<Any?, Value> =
        ValueDelegate(this, valueName, def) { it }

fun Meta.stringValue(valueName: String? = null, def: String? = null): ReadOnlyProperty<Any?, String> =
        ValueDelegate(this, valueName, def) { it.string }

fun Meta.booleanValue(valueName: String? = null, def: Boolean? = null): ReadOnlyProperty<Any?, Boolean> =
        ValueDelegate(this, valueName, def) { it.boolean }

fun Meta.timeValue(valueName: String? = null, def: Instant? = null): ReadOnlyProperty<Any?, Instant> =
        ValueDelegate(this, valueName, def) { it.time }

fun Meta.numberValue(valueName: String? = null, def: Number? = null): ReadOnlyProperty<Any?, Number> =
        ValueDelegate(this, valueName, def) { it.number }

fun Meta.doubleValue(valueName: String? = null, def: Double? = null): ReadOnlyProperty<Any?, Double> =
        ValueDelegate(this, valueName, def) { it.double }

fun Meta.intValue(valueName: String? = null, def: Int? = null): ReadOnlyProperty<Any?, Int> =
        ValueDelegate(this, valueName, def) { it.int }

fun <T> Meta.customValue(valueName: String? = null, def: T? = null, conv: (Value) -> T): ReadOnlyProperty<Any?, T> =
        ValueDelegate(this, valueName, def, conv)

fun Meta.node(nodeName: String? = null, def: Meta? = null): ReadOnlyProperty<Any, Meta> =
        NodeDelegate(this, nodeName, def) { it }

fun <T> Meta.customNode(nodeName: String? = null, def: T? = null, conv: (Meta) -> T): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def, conv)

fun <T : MetaMorph> Meta.morphNode(type: KClass<T>, nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def) { MetaMorph.morph(type, it) }

inline fun <reified T : MetaMorph> Meta.morphNode(nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        morphNode(T::class, nodeName, def)

//Metoid extensions

fun Metoid.value(valueName: String? = null, def: Value? = null): ReadOnlyProperty<Any?, Value> =
        ValueDelegate(meta, valueName, def) { it }

fun Metoid.stringValue(valueName: String? = null, def: String? = null): ReadOnlyProperty<Any?, String> =
        ValueDelegate(meta, valueName, def) { it.string }

fun Metoid.booleanValue(valueName: String? = null, def: Boolean? = null): ReadOnlyProperty<Any?, Boolean> =
        ValueDelegate(meta, valueName, def) { it.boolean }

fun Metoid.timeValue(valueName: String? = null, def: Instant? = null): ReadOnlyProperty<Any?, Instant> =
        ValueDelegate(meta, valueName, def) { it.time }

fun Metoid.numberValue(valueName: String? = null, def: Number? = null): ReadOnlyProperty<Any?, Number> =
        ValueDelegate(meta, valueName, def) { it.number }

fun Metoid.doubleValue(valueName: String? = null, def: Double? = null): ReadOnlyProperty<Any?, Double> =
        ValueDelegate(meta, valueName, def) { it.double }

fun Metoid.intValue(valueName: String? = null, def: Int? = null): ReadOnlyProperty<Any?, Int> =
        ValueDelegate(meta, valueName, def) { it.int }

fun <T> Metoid.customValue(valueName: String? = null, def: T? = null, conv: (Value) -> T): ReadOnlyProperty<Any?, T> =
        ValueDelegate(meta, valueName, def, conv)

fun Metoid.node(nodeName: String? = null, def: Meta? = null): ReadOnlyProperty<Any, Meta> =
        NodeDelegate(meta, nodeName, def) { it }

fun <T> Metoid.customNode(nodeName: String? = null, def: T? = null, conv: (Meta) -> T): ReadOnlyProperty<Any, T> =
        NodeDelegate(meta, nodeName, def, conv)

fun <T : MetaMorph> Metoid.morphNode(type: KClass<T>, nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        NodeDelegate(meta, nodeName, def) { MetaMorph.morph(type, it) }

inline fun <reified T : MetaMorph> Metoid.morphNode(nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
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
        MutableValueDelegate(this, valueName, def, { it.string }, { it.parseValue() })

fun MutableMetaNode<*>.mutableBooleanValue(valueName: String? = null, def: Boolean? = null): ReadWriteProperty<Any, Boolean> =
        MutableValueDelegate(this, valueName, def, { it.boolean }, { Value.of(it) })

fun MutableMetaNode<*>.mutableTimeValue(valueName: String? = null, def: Instant? = null): ReadWriteProperty<Any, Instant> =
        MutableValueDelegate(this, valueName, def, { it.time }, { Value.of(it) })

fun MutableMetaNode<*>.mutableNumberValue(valueName: String? = null, def: Number? = null): ReadWriteProperty<Any, Number> =
        MutableValueDelegate(this, valueName, def, { it.number }, { Value.of(it) })

fun MutableMetaNode<*>.mutableDoubleValue(valueName: String? = null, def: Double? = null): ReadWriteProperty<Any, Double> =
        MutableValueDelegate(this, valueName, def, { it.double }, { Value.of(it) })

fun MutableMetaNode<*>.mutableIntValue(valueName: String? = null, def: Int? = null): ReadWriteProperty<Any, Int> =
        MutableValueDelegate(this, valueName, def, { it.int }, { Value.of(it) })

fun <T> MutableMetaNode<*>.mutableCustomValue(valueName: String? = null, def: T? = null, read: (Value) -> T, write: (T) -> Any): ReadWriteProperty<Any, T> =
        MutableValueDelegate(this, valueName, def, read, write)

/**
 * Returns a child node of given meta that could be edited in-place
 */
fun MetaBuilder.mutableNode(metaName: String? = null, def: Meta? = null): ReadWriteProperty<Any, MetaBuilder> =
        MutableMetaDelegate(this, metaName, MetaBuilder(def ?: Meta.empty()), {
            it as? MetaBuilder ?: it.builder
        }, { it })

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


//ValueProvider delegates

/**
 * Delegate class for valueProvider
 */


//private class ValueProviderDelegate<T>(private val valueName: String?, val conv: (Value) -> T) : ReadOnlyProperty<ValueProvider, T> {
//    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): T =
//            conv(thisRef.getValue(valueName ?: property.name))
//}
//
///**
// * Delegate ValueProvider element to read only property
// */
//fun ValueProvider.valueDelegate(valueName: String? = null): ReadOnlyProperty<ValueProvider, Value> = ValueProviderDelegate(valueName) { it }

//
//fun ValueProvider.getString(valueName: String? = null): ReadOnlyProperty<ValueProvider, String> = ValueProviderDelegate(valueName) { it.getString() }
//fun ValueProvider.booleanValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, Boolean> = ValueProviderDelegate(valueName) { it.booleanValue() }
//fun ValueProvider.getTime(valueName: String? = null): ReadOnlyProperty<ValueProvider, Instant> = ValueProviderDelegate(valueName) { it.getTime() }
//fun ValueProvider.numberValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, Number> = ValueProviderDelegate(valueName) { it.numberValue() }
//fun <T> ValueProvider.customValue(valueName: String? = null, conv: (Value) -> T): ReadOnlyProperty<ValueProvider, T> = ValueProviderDelegate(valueName, conv)

//Meta provider delegate

//private class MetaDelegate(private val metaName: String?) : ReadOnlyProperty<MetaProvider, Meta> {
//    override operator fun getValue(thisRef: MetaProvider, property: KProperty<*>): Meta =
//            thisRef.optMeta(metaName ?: property.name).orElse(null);
//}
//
//fun MetaProvider.metaNode(metaName: String? = null): ReadOnlyProperty<MetaProvider, Meta> = MetaDelegate(metaName)
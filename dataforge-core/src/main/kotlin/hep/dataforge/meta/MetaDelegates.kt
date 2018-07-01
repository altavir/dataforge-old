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

package hep.dataforge.meta

import hep.dataforge.description.Described
import hep.dataforge.values.Value
import hep.dataforge.values.parseValue
import java.time.Instant
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/* Meta delegate classes */

interface MetaDelegate {
    val target: Meta
    val name: String?
}

/**
 * The delegate for value in meta
 * Values for sealed meta are automatically cached
 * @property name name of the property. If null, use property name
 */
open class ValueDelegate<T : Any>(
        override val target: Meta,
        override val name: String? = null,
        val def: T? = null,
        val write: (T) -> Any = { it },
        val read: (Value) -> T
) : ReadOnlyProperty<Any?, T>, MetaDelegate {

    private var cached: T? = null

    private fun getValueInternal(thisRef: Any?, property: KProperty<*>): T {
        val key = name ?: property.name
        return when {
            target.hasValue(key) -> read(target.getValue(key))
            def != null -> def
            thisRef is Described -> thisRef.descriptor.getValueDescriptor(key)?.default?.let(read)
                    ?: throw RuntimeException("Neither value, not default found for value $key in $thisRef")
            else -> throw RuntimeException("Neither value, not default found for value $key in $thisRef")
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (target is SealedNode && cached == null) {
            cached = getValueInternal(thisRef, property)
        }
        return cached ?: getValueInternal(thisRef, property)
    }

    val defaultValue: Any
        get() = def?.let { write(it) } ?: Value.NULL
}

class EnumValueDelegate<T : Enum<T>>(
        target: Meta,
        val type: KClass<T>,
        name: String? = null,
        def: T? = null
) : ValueDelegate<T>(target, name, def, write = { it.name }, read = { java.lang.Enum.valueOf(type.java, it.string) })

open class NodeDelegate<T>(
        override val target: Meta,
        override val name: String? = null,
        val def: T? = null,
        val read: (Meta) -> T
) : ReadOnlyProperty<Any, T>, MetaDelegate {

    private var cached: T? = null

    private fun getNodeInternal(thisRef: Any, property: KProperty<*>): T {
        val key = name ?: property.name
        return when {
            target.hasMeta(key) -> read(target.getMeta(key))
            def != null -> def
            thisRef is Described -> thisRef.descriptor.getNodeDescriptor(key)?.default?.firstOrNull()?.let(read)
                    ?: throw RuntimeException("Neither value, not default found for node $key in $thisRef")
            else -> throw RuntimeException("Neither value, not default found for node $key in $thisRef")
        }
    }


    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (target is SealedNode && cached == null) {
            cached = getNodeInternal(thisRef, property)
        }
        return cached ?: getNodeInternal(thisRef, property)
    }
}

class NodeListDelegate<out T>(
        override val target: Meta,
        override val name: String?,
        val def: List<T>? = null,
        private val read: (Meta) -> T
) : ReadOnlyProperty<Any?, List<T>>, MetaDelegate {

    private var cached: List<T>? = null

    private fun getNodeInternal(thisRef: Any?, property: KProperty<*>): List<T> {
        val key = name ?: property.name
        return when {
            target.hasMeta(key) -> target.getMetaList(key).map(read)
            def != null -> def
            thisRef is Described -> thisRef.descriptor.getNodeDescriptor(key)?.default?.map(read)
                    ?: throw RuntimeException("Neither value, not default found for node $key in $thisRef")
            else -> throw RuntimeException("Neither value, not default found for node $key in $thisRef")
        }
    }


    override operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        if (target is SealedNode && cached == null) {
            cached = getNodeInternal(thisRef, property)
        }
        return cached ?: getNodeInternal(thisRef, property)
    }
}

open class MutableValueDelegate<T : Any>(
        override val target: MutableMetaNode<*>,
        name: String? = null,
        def: T? = null,
        write: (T) -> Any = { it },
        read: (Value) -> T
) : ReadWriteProperty<Any?, T>, ValueDelegate<T>(target, name, def, write, read) {

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        //TODO set for allowed values
        target.setValue(name ?: property.name, write(value))
    }
}

class MutableEnumValueDelegate<T : Enum<T>>(
        target: MutableMetaNode<*>,
        type: KClass<T>,
        name: String? = null,
        def: T? = null
) : MutableValueDelegate<T>(target, name, def, write = { it.name }, read = { java.lang.Enum.valueOf(type.java, it.string) })

class MutableNodeDelegate<T>(
        override val target: MutableMetaNode<*>,
        name: String?,
        def: T? = null,
        val write: (T) -> Meta,
        read: (Meta) -> T
) : ReadWriteProperty<Any, T>, NodeDelegate<T>(target, name, def, read) {

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        target.setNode(name ?: property.name, write(value))
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

fun <T : Any> Meta.customValue(valueName: String? = null, def: T? = null, conv: (Value) -> T): ReadOnlyProperty<Any?, T> =
        ValueDelegate(this, valueName, def, read = conv)

inline fun <reified T : Enum<T>> Meta.enumValue(valueName: String? = null, def: T? = null): ReadOnlyProperty<Any?, T> =
        EnumValueDelegate(this, T::class, valueName, def)

fun Meta.node(nodeName: String? = null, def: Meta? = null): ReadOnlyProperty<Any, Meta> =
        NodeDelegate(this, nodeName, def) { it }

fun Meta.nodeList(nodeName: String? = null, def: List<Meta>? = null): ReadOnlyProperty<Any, List<Meta>> =
        NodeListDelegate(this, nodeName, def) { it }

fun <T> Meta.customNode(nodeName: String? = null, def: T? = null, conv: (Meta) -> T): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def, conv)

fun <T : MetaMorph> Meta.morph(type: KClass<T>, nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        NodeDelegate(this, nodeName, def) { MetaMorph.morph(type, it) }

fun <T : MetaMorph> Meta.morphList(type: KClass<T>, nodeName: String? = null, def: List<T>? = null): ReadOnlyProperty<Any, List<T>> =
        NodeListDelegate(this, nodeName, def) { MetaMorph.morph(type, it) }

inline fun <reified T : MetaMorph> Meta.morph(nodeName: String? = null, def: T? = null): ReadOnlyProperty<Any, T> =
        this.morph(T::class, nodeName, def)

inline fun <reified T : MetaMorph> Meta.morphList(nodeName: String? = null, def: List<T>? = null): ReadOnlyProperty<Any, List<T>> =
        NodeListDelegate(this, nodeName, def) { MetaMorph.morph(T::class, it) }

//Metoid extensions

fun Metoid.value(valueName: String? = null, def: Value? = null) = meta.value(valueName, def)
fun Metoid.stringValue(valueName: String? = null, def: String? = null) = meta.stringValue(valueName, def)
fun Metoid.booleanValue(valueName: String? = null, def: Boolean? = null) = meta.booleanValue(valueName, def)
fun Metoid.timeValue(valueName: String? = null, def: Instant? = null) = meta.timeValue(valueName, def)
fun Metoid.numberValue(valueName: String? = null, def: Number? = null) = meta.numberValue(valueName, def)
fun Metoid.doubleValue(valueName: String? = null, def: Double? = null) = meta.doubleValue(valueName, def)
fun Metoid.intValue(valueName: String? = null, def: Int? = null) = meta.intValue(valueName, def)
fun <T : Any> Metoid.customValue(valueName: String? = null, def: T? = null, read: (Value) -> T) = meta.customValue(valueName, def, read)
inline fun <reified T : Enum<T>> Metoid.enumValue(valueName: String? = null, def: T? = null) = meta.enumValue(valueName, def)
fun Metoid.node(nodeName: String? = null, def: Meta? = null) = meta.node(nodeName, def)
fun Metoid.nodeList(nodeName: String? = null, def: List<Meta>? = null) = meta.nodeList(nodeName, def)
fun <T> Metoid.customNode(nodeName: String? = null, def: T? = null, conv: (Meta) -> T) = meta.customNode(nodeName, def, conv)
fun <T : MetaMorph> Metoid.morph(type: KClass<T>, nodeName: String? = null, def: T? = null) = meta.morph(type, nodeName, def)
inline fun <reified T : MetaMorph> Metoid.morph(nodeName: String? = null, def: T? = null) = meta.morph<T>(nodeName, def)
inline fun <reified T : MetaMorph> Metoid.morphList(nodeName: String? = null, def: List<T>? = null) = meta.morphList<T>(nodeName, def)

//Configuration extension

/* [MutableMetaNode] extensions */

fun MutableMetaNode<*>.mutableValue(valueName: String? = null, def: Value? = null): ReadWriteProperty<Any?, Value> =
        MutableValueDelegate(this, valueName, def, { it }, { it })

fun MutableMetaNode<*>.mutableStringValue(valueName: String? = null, def: String? = null): ReadWriteProperty<Any?, String> =
        MutableValueDelegate(this, valueName, def, { it.parseValue() }, Value::string)

fun MutableMetaNode<*>.mutableBooleanValue(valueName: String? = null, def: Boolean? = null): ReadWriteProperty<Any, Boolean> =
        MutableValueDelegate(this, valueName, def) { it.boolean }

fun MutableMetaNode<*>.mutableTimeValue(valueName: String? = null, def: Instant? = null): ReadWriteProperty<Any, Instant> =
        MutableValueDelegate(this, valueName, def) { it.time }

fun MutableMetaNode<*>.mutableNumberValue(valueName: String? = null, def: Number? = null): ReadWriteProperty<Any, Number> =
        MutableValueDelegate(this, valueName, def) { it.number }

fun MutableMetaNode<*>.mutableDoubleValue(valueName: String? = null, def: Double? = null): ReadWriteProperty<Any, Double> =
        MutableValueDelegate(this, valueName, def) { it.double }

fun MutableMetaNode<*>.mutableIntValue(valueName: String? = null, def: Int? = null): ReadWriteProperty<Any, Int> =
        MutableValueDelegate(this, valueName, def) { it.int }

fun <T : Any> MutableMetaNode<*>.mutableCustomValue(valueName: String? = null, def: T? = null, read: (Value) -> T, write: (T) -> Any): ReadWriteProperty<Any, T> =
        MutableValueDelegate(this, valueName, def, write, read)

/**
 * Returns a child node of given meta that could be edited in-place
 */
fun MutableMetaNode<*>.mutableNode(metaName: String? = null, def: Meta? = null): ReadWriteProperty<Any, MetaBuilder> =
        MutableNodeDelegate(this, metaName, MetaBuilder(def ?: Meta.empty()),
                read = {
                    it as? MetaBuilder ?: it.builder
                },
                write = { it }
        )

fun <T> MutableMetaNode<*>.mutableCustomNode(metaName: String? = null, def: T? = null, write: (T) -> Meta, read: (Meta) -> T): ReadWriteProperty<Any, T> =
        MutableNodeDelegate(this, metaName, def, write, read)

/**
 * Create a property that is delegate for configurable
 */
fun <T : MetaMorph> MutableMetaNode<*>.mutableMorph(type: KClass<T>, metaName: String? = null, def: T? = null): ReadWriteProperty<Any, T> {
    return MutableNodeDelegate(this, metaName, def, { it.toMeta() }, { MetaMorph.morph(type, it) })
}

inline fun <reified T : MetaMorph> MutableMetaNode<*>.mutableMorph(metaName: String? = null, def: T? = null): ReadWriteProperty<Any, T> =
        mutableMorph(T::class, metaName, def)


/* [Configurable] extensions */

fun Configurable.value(valueName: String? = null, def: Value? = null) = config.mutableValue(valueName, def)
fun Configurable.stringValue(valueName: String? = null, def: String? = null) = config.mutableStringValue(valueName, def)
fun Configurable.booleanValue(valueName: String? = null, def: Boolean? = null) = config.mutableBooleanValue(valueName, def)
fun Configurable.timeValue(valueName: String? = null, def: Instant? = null) = config.mutableTimeValue(valueName, def)
fun Configurable.numberValue(valueName: String? = null, def: Number? = null) = config.mutableNumberValue(valueName, def)
fun Configurable.doubleValue(valueName: String? = null, def: Double? = null) = config.mutableDoubleValue(valueName, def)
fun Configurable.intValue(valueName: String? = null, def: Int? = null) = config.mutableIntValue(valueName, def)
fun <T : Any> Configurable.customValue(valueName: String? = null, def: T? = null, read: (Value) -> T, write: (T) -> Any) =
        config.mutableCustomValue(valueName, def, read, write)

fun Configurable.node(nodeName: String? = null, def: Meta? = null) = config.mutableNode(nodeName, def)
fun <T> Configurable.customNode(nodeName: String? = null, def: T? = null, write: (T) -> Meta, read: (Meta) -> T) =
        config.mutableCustomNode(nodeName, def, write, read)

fun <T : MetaMorph> Configurable.morph(type: KClass<T>, nodeName: String? = null, def: T? = null) = config.mutableMorph(type, nodeName, def)
inline fun <reified T : MetaMorph> Configurable.morph(nodeName: String? = null, def: T? = null) = config.mutableMorph(nodeName, def)


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
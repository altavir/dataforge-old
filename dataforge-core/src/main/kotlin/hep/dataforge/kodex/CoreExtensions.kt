package hep.dataforge.kodex

import hep.dataforge.context.Context
import hep.dataforge.context.Plugin
import hep.dataforge.data.Data
import hep.dataforge.data.NamedData
import hep.dataforge.description.Descriptors
import hep.dataforge.goals.Goal
import hep.dataforge.goals.StaticGoal
import hep.dataforge.meta.*
import hep.dataforge.values.NamedValue
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import hep.dataforge.values.ValueType
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.future.await
import java.lang.reflect.AnnotatedElement
import java.time.Instant
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Core DataForge classes extensions
 * Created by darksnake on 26-Apr-17.
 */

// Context extensions
val global: Context = hep.dataforge.context.Global.instance()

/**
 * Build a child plugin using given name, plugins list and custom build script
 */
fun Context.buildContext(name: String, vararg plugins: Class<out Plugin>, init: Context.Builder.() -> Unit = {}): Context {
    val builder = Context.Builder(this, name)
    plugins.forEach {
        builder.plugin(it)
    }
    builder.apply(init)
    return builder.build()
}

fun buildContext(name: String, vararg plugins: Class<out Plugin>, init: Context.Builder.() -> Unit = {}): Context {
    return global.buildContext(name = name, plugins = *plugins, init = init)
}

//Value operations

operator fun Value.plus(other: Value): Value =
        when (this.type) {
            ValueType.NUMBER -> Value.of(this.numberValue() + other.numberValue());
            ValueType.STRING -> Value.of(this.stringValue() + other.stringValue());
            ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.timeValue().toEpochMilli() + other.timeValue().toEpochMilli()))
            ValueType.BOOLEAN -> Value.of(this.booleanValue() || other.booleanValue());
            ValueType.NULL -> other;
        }

operator fun Value.minus(other: Value): Value =
        when (this.type) {
            ValueType.NUMBER -> Value.of(this.numberValue() - other.numberValue());
            ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.timeValue().toEpochMilli() - other.timeValue().toEpochMilli()))
            else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
        }

operator fun Value.times(other: Value): Value =
        when (this.type) {
            ValueType.NUMBER -> Value.of(this.numberValue() * other.numberValue());
            else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
        }

operator fun Value.plus(other: Any): Value = this + Value.of(other)

operator fun Value.minus(other: Any): Value = this - Value.of(other)

operator fun Value.times(other: Any): Value = this * Value.of(other)

//Value comparison

operator fun Value.compareTo(other: Value): Int = when (this.type) {
    ValueType.NUMBER -> this.numberValue().compareTo(other.numberValue());
    ValueType.STRING -> this.stringValue().compareTo(other.stringValue())
    ValueType.TIME -> this.timeValue().compareTo(other.timeValue())
    ValueType.BOOLEAN -> this.booleanValue().compareTo(other.booleanValue())
    ValueType.NULL -> if (other.isNull) 0 else 1
}

fun Value?.isNull(): Boolean = this == null || this.isNull

//ValueProvider delegates

/**
 * Delegate class for valueProvider
 */


private class ValueProviderDelegate<T>(private val valueName: String?, val conv: (Value) -> T) : ReadOnlyProperty<ValueProvider, T> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): T =
            conv(thisRef.getValue(valueName ?: property.name))
}

/**
 * Delegate ValueProvider element to read only property
 */
fun ValueProvider.valueDelegate(valueName: String? = null): ReadOnlyProperty<ValueProvider, Value> = ValueProviderDelegate(valueName) { it }

//
//fun ValueProvider.stringValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, String> = ValueProviderDelegate(valueName) { it.stringValue() }
//fun ValueProvider.booleanValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, Boolean> = ValueProviderDelegate(valueName) { it.booleanValue() }
//fun ValueProvider.timeValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, Instant> = ValueProviderDelegate(valueName) { it.timeValue() }
//fun ValueProvider.numberValue(valueName: String? = null): ReadOnlyProperty<ValueProvider, Number> = ValueProviderDelegate(valueName) { it.numberValue() }
//fun <T> ValueProvider.customValue(valueName: String? = null, conv: (Value) -> T): ReadOnlyProperty<ValueProvider, T> = ValueProviderDelegate(valueName, conv)


//Meta operations

operator fun Meta.get(path: String): Value = this.getValue(path)

operator fun <T : MutableMetaNode<*>> MutableMetaNode<T>.set(path: String, value: Any): T = this.setValue(path, value)

operator fun <T : MutableMetaNode<*>> T.plusAssign(value: NamedValue) {
    this.setValue(value.name, value.anonymousValue);
}

operator fun <T : MutableMetaNode<*>> T.plusAssign(meta: Meta) {
    this.putNode(meta);
}

/**
 * Create a new meta with added node
 */
operator fun Meta.plus(meta: Meta): Meta = this.builder.putNode(meta)

/**
 * create a new meta with added value
 */
operator fun Meta.plus(value: NamedValue): Meta = this.builder.putValue(value.name, value.anonymousValue)

/**
 * Get a value if it is present and apply action to it
 */
fun ValueProvider.useValue(valueName: String, action: (Value) -> Unit) {
    optValue(valueName).ifPresent(action)
}

/**
 * Get a meta node if it is present and apply action to it
 */
fun MetaProvider.useMeta(metaName: String, action: (Meta) -> Unit) {
    optMeta(metaName).ifPresent(action)
}

/**
 * Perform some action on each meta element of the list if it is present
 */
fun Meta.useEachMeta(metaName: String, action: (Meta) -> Unit) {
    if (hasMeta(metaName)) {
        getMetaList(metaName).forEach(action)
    }
}

/**
 * Get all meta nodes with the given name and apply action to them
 */
fun Meta.useMetaList(metaName: String, action: (List<Meta>) -> Unit) {
    if (hasMeta(metaName)) {
        action(getMetaList(metaName))
    }
}

//Meta provider delegate

class MetaDelegate(private val metaName: String?) : ReadOnlyProperty<MetaProvider, Meta?> {
    override operator fun getValue(thisRef: MetaProvider, property: KProperty<*>): Meta? =
            thisRef.optMeta(metaName ?: property.name).orElse(null);
}

fun MetaProvider.metaNode(metaName: String? = null): MetaDelegate = MetaDelegate(metaName)

//Metoid values


class MetoidValueDelegate<T>(private val valueName: String?, val conv: (Value) -> T) : ReadOnlyProperty<Metoid, T> {
    //TODO add caching
    override operator fun getValue(thisRef: Metoid, property: KProperty<*>): T =
            conv(Descriptors.getDelegatedValue(thisRef.meta, valueName, thisRef, property))
}

class MetoidNodeDelegate<T>(private val nodeName: String?, val conv: (Meta) -> T) : ReadOnlyProperty<Metoid, T> {
    override operator fun getValue(thisRef: Metoid, property: KProperty<*>): T =
            conv(Descriptors.getDelegatedMeta(thisRef.meta, nodeName, thisRef, property))
}

/**
 * Delegate MutableMetaNode element to read/write property
 */
fun Metoid.value(valueName: String? = null): ReadOnlyProperty<Metoid, Value> = MetoidValueDelegate(valueName) { it }

fun Metoid.stringValue(valueName: String? = null): ReadOnlyProperty<Metoid, String> = MetoidValueDelegate(valueName) { it.stringValue() }
fun Metoid.booleanValue(valueName: String? = null): ReadOnlyProperty<Metoid, Boolean> = MetoidValueDelegate(valueName) { it.booleanValue() }
fun Metoid.timeValue(valueName: String? = null): ReadOnlyProperty<Metoid, Instant> = MetoidValueDelegate(valueName) { it.timeValue() }
fun Metoid.numberValue(valueName: String? = null): ReadOnlyProperty<Metoid, Number> = MetoidValueDelegate(valueName) { it.numberValue() }
fun Metoid.doubleValue(valueName: String? = null): ReadOnlyProperty<Metoid, Double> = MetoidValueDelegate(valueName) { it.doubleValue() }
fun Metoid.intValue(valueName: String? = null): ReadOnlyProperty<Metoid, Int> = MetoidValueDelegate(valueName) { it.intValue() }
fun <T> Metoid.customValue(valueName: String? = null, conv: (Value) -> T): ReadOnlyProperty<Metoid, T> = MetoidValueDelegate(valueName, conv)
fun Metoid.node(nodeName: String? = null): ReadOnlyProperty<Metoid, Meta> = MetoidNodeDelegate(nodeName) { it }
fun <T> Metoid.customNode(nodeName: String? = null, conv: (Meta) -> T): ReadOnlyProperty<Metoid, T> = MetoidNodeDelegate(nodeName, conv)

inline fun <reified T : MetaMorph> Metoid.morph(nodeName: String? = null): ReadOnlyProperty<Metoid, T> =
        MetoidNodeDelegate(nodeName) { MetaMorph.morph(T::class, it) }

//Annotations

fun <T : Annotation> listAnnotations(source: AnnotatedElement, type: Class<T>, searchSuper: Boolean): List<T> {
    if (source is Class<*>) {
        val res = ArrayList<T>()
        val array = source.getDeclaredAnnotationsByType(type)
        res.addAll(Arrays.asList(*array))
        if (searchSuper) {
            val superClass = source.superclass
            if (superClass != null) {
                res.addAll(listAnnotations(superClass, type, true))
            }
            for (cl in source.interfaces) {
                res.addAll(listAnnotations(cl, type, true))
            }
        }
        return res;
    } else {
        val array = source.getAnnotationsByType(type)
        return Arrays.asList(*array)
    }
}

//Configuration extension

class ConfigurableValueDelegate<T>(private val valueName: String?, val toT: (Value) -> T, val toValue: (T) -> Any) : ReadWriteProperty<Configurable, T> {
    //TODO add caching
    override operator fun getValue(thisRef: Configurable, property: KProperty<*>): T =
            toT(Descriptors.getDelegatedValue(thisRef.config, valueName, thisRef, property))

    override fun setValue(thisRef: Configurable, property: KProperty<*>, value: T) {
        Descriptors.setDelegatedValue(thisRef.config, valueName, Value.of(toValue(value)), thisRef, property)
    }
}

class ConfigurableMetaDelegate<T>(private val metaName: String?, val read: (Meta) -> T, val write: (T) -> Meta) : ReadWriteProperty<Configurable, T> {
    override operator fun getValue(thisRef: Configurable, property: KProperty<*>): T {
        return read(Descriptors.getDelegatedMeta(thisRef.config, metaName, thisRef, property))
    }

    override operator fun setValue(thisRef: Configurable, property: KProperty<*>, value: T) {
        thisRef.config.setNode(metaName ?: property.name, write(value))
    }
}

fun Configurable.value(valueName: String? = null): ReadWriteProperty<Configurable, Value> =
        ConfigurableValueDelegate(valueName, { it }, { it })

fun Configurable.stringValue(valueName: String? = null): ReadWriteProperty<Configurable, String> =
        ConfigurableValueDelegate(valueName, { it.stringValue() }, { Value.of(it) })

fun Configurable.booleanValue(valueName: String? = null): ReadWriteProperty<Configurable, Boolean> =
        ConfigurableValueDelegate(valueName, { it.booleanValue() }, { Value.of(it) })

fun Configurable.timeValue(valueName: String? = null): ReadWriteProperty<Configurable, Instant> =
        ConfigurableValueDelegate(valueName, { it.timeValue() }, { Value.of(it) })

fun Configurable.numberValue(valueName: String? = null): ReadWriteProperty<Configurable, Number> =
        ConfigurableValueDelegate(valueName, { it.numberValue() }, { Value.of(it) })

fun Configurable.doubleValue(valueName: String? = null): ReadWriteProperty<Configurable, Double> =
        ConfigurableValueDelegate(valueName, { it.doubleValue() }, { Value.of(it) })

fun Configurable.intValue(valueName: String? = null): ReadWriteProperty<Configurable, Int> =
        ConfigurableValueDelegate(valueName, { it.intValue() }, { Value.of(it) })

fun <T> Configurable.customValue(valueName: String? = null, read: (Value) -> T, write: (T) -> Any): ReadWriteProperty<Configurable, T> =
        ConfigurableValueDelegate(valueName, read, write)

fun Configurable.node(metaName: String? = null): ReadWriteProperty<Configurable, Meta> =
        ConfigurableMetaDelegate(metaName, { it }, { it })

fun <T> Configurable.customNode(metaName: String? = null, read: (Meta) -> T, write: (T) -> Meta): ReadWriteProperty<Configurable, T> =
        ConfigurableMetaDelegate(metaName, read, write)

/**
 * Create a property that is delegate for configurable
 */
inline fun <reified T : MetaMorph> Configurable.morph(metaName: String? = null): ReadWriteProperty<Configurable, T> =
        ConfigurableMetaDelegate(metaName, { MetaMorph.morph(T::class, it) }, { it.toMeta() })

/**
 * Configure a configurable using in-place build meta
 */
fun <T : Configurable> T.configure(transform: KMetaBuilder.() -> Unit): T {
    this.configure(hep.dataforge.kodex.buildMeta(this.config.name, transform));
    return this;
}

//suspending functions

val Context.coroutineContext: CoroutineContext
    get() = optFeature(KodexPlugin::class.java).map { it.dispatcher }.orElse(DefaultDispatcher)

/**
 * Use goal as a suspending function
 */
suspend fun <R> Goal<R>.await(): R {
    return when {
        this is Coal<R> -> this.await()//A special case for Coal
        this is StaticGoal<R> -> this.get()//optimization for static goals
        else -> this.result().await()
    }
}

inline fun <T, reified R> Data<T>.pipe(dispatcher: CoroutineContext, noinline transform: suspend (T) -> R): Data<R> =
        Data(this.goal.pipe(dispatcher, transform), R::class.java, this.meta)

inline fun <T, reified R> NamedData<T>.pipe(dispatcher: CoroutineContext, noinline transform: suspend (T) -> R): NamedData<R> =
        NamedData(this.name, this.goal.pipe(dispatcher, transform), R::class.java, this.meta)
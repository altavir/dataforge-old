package hep.dataforge.kodex

import hep.dataforge.context.Context
import hep.dataforge.context.ContextBuilder
import hep.dataforge.context.Global
import hep.dataforge.context.Plugin
import hep.dataforge.data.Data
import hep.dataforge.data.NamedData
import hep.dataforge.goals.Goal
import hep.dataforge.goals.StaticGoal
import hep.dataforge.meta.*
import hep.dataforge.values.NamedValue
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import hep.dataforge.values.ValueType
import kotlinx.coroutines.experimental.future.await
import java.lang.reflect.AnnotatedElement
import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Core DataForge classes extensions
 * Created by darksnake on 26-Apr-17.
 */

// Context extensions

/**
 * Build a child plugin using given name, plugins list and custom build script
 */
fun Context.buildContext(name: String, vararg plugins: Class<out Plugin>, init: ContextBuilder.() -> Unit = {}): Context {
    val builder = ContextBuilder(name, this)
    plugins.forEach {
        builder.plugin(it)
    }
    builder.apply(init)
    return builder.build()
}

fun buildContext(name: String, vararg plugins: Class<out Plugin>, init: ContextBuilder.() -> Unit = {}): Context {
    return Global.buildContext(name = name, plugins = *plugins, init = init)
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

fun <T> Meta.asMap(transform: (Value) -> T): Map<String, T> {
    return MetaUtils.valueStream(this).collect(Collectors.toMap({ it.key }, { transform(it.value) }))
}

//Meta provider delegate

private class MetaDelegate(private val metaName: String?) : ReadOnlyProperty<MetaProvider, Meta> {
    override operator fun getValue(thisRef: MetaProvider, property: KProperty<*>): Meta =
            thisRef.optMeta(metaName ?: property.name).orElse(null);
}

fun MetaProvider.metaNode(metaName: String? = null): ReadOnlyProperty<MetaProvider, Meta> = MetaDelegate(metaName)


/**
 * Configure a configurable using in-place build meta
 */
fun <T : Configurable> T.configure(transform: KMetaBuilder.() -> Unit): T {
    this.configure(hep.dataforge.kodex.buildMeta(this.config.name, transform));
    return this;
}

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


//suspending functions

val Context.coroutineContext: CoroutineContext
    get() = this.executor.kDispatcher

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
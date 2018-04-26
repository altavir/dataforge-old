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
import hep.dataforge.names.Names
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
            ValueType.NUMBER -> Value.of(this.number + other.number);
            ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.time.toEpochMilli() + other.time.toEpochMilli()))
            ValueType.NULL -> other;
            else -> throw RuntimeException("Operation plus not allowed for ${this.type}");
        }

operator fun Value.minus(other: Value): Value =
        when (this.type) {
            ValueType.NUMBER -> Value.of(this.number - other.number);
            ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.time.toEpochMilli() - other.time.toEpochMilli()))
            else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
        }

operator fun Value.times(other: Value): Value =
        when (this.type) {
            ValueType.NUMBER -> Value.of(this.number * other.number);
            else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
        }

operator fun Value.plus(other: Any): Value = this + Value.of(other)

operator fun Value.minus(other: Any): Value = this - Value.of(other)

operator fun Value.times(other: Any): Value = this * Value.of(other)

//Value comparison

operator fun Value.compareTo(other: Value): Int = when (this.type) {
    ValueType.NUMBER -> this.number.compareTo(other.number);
    ValueType.STRING -> this.string.compareTo(other.string)
    ValueType.TIME -> this.time.compareTo(other.time)
    ValueType.BOOLEAN -> this.boolean.compareTo(other.boolean)
    ValueType.NULL -> if (other.isNull) 0 else 1
    ValueType.BINARY -> this.binary.compareTo(other.binary)
}

fun Value?.isNull(): Boolean = this == null || this.isNull


//Meta operations

operator fun Meta.get(path: String): Value = this.getValue(path)

operator fun Value.get(index: Int): Value = this.list[index]

operator fun <T : MutableMetaNode<*>> MutableMetaNode<T>.set(path: String, value: Any): T = this.setValue(path, value)

operator fun <T : MutableMetaNode<*>> T.plusAssign(value: NamedValue) {
    this.setValue(value.name, value.anonymous);
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
operator fun Meta.plus(value: NamedValue): Meta = this.builder.putValue(value.name, value.anonymous)

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
    return MetaUtils.valueStream(this).collect(Collectors.toMap({ it.first }, { transform(it.second) }))
}

val <T : MetaNode<*>> MetaNode<T>.childNodes: List<T>
    get() = this.nodeNames.map { this.getMeta(it) }.toList()

val Meta.childNodes: List<Meta>
    get() = this.nodeNames.map { this.getMeta(it) }.toList()


/**
 * Configure a configurable using in-place build meta
 */
fun <T : Configurable> T.configure(transform: KMetaBuilder.() -> Unit): T {
    this.configure(hep.dataforge.kodex.buildMeta(this.config.name, transform));
    return this;
}

//Annotations

fun <T : Annotation> listAnnotations(source: AnnotatedElement, type: Class<T>, searchSuper: Boolean = true): List<T> {
    if (source is Class<*>) {
        val res = ArrayList<T>()
        val array = source.getAnnotationsByType(type)
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
    get() = this.executors.kDispatcher

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
        Data(R::class.java, this.goal.pipe(dispatcher, transform), this.meta)

inline fun <T, reified R> NamedData<T>.pipe(dispatcher: CoroutineContext, noinline transform: suspend (T) -> R): NamedData<R> =
        NamedData(this.name, R::class.java, this.goal.pipe(dispatcher, transform), this.meta)

operator fun Names.plus(other: Names): Names {
    return this.plus(*other.asArray())
}
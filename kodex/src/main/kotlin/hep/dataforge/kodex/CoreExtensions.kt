package hep.dataforge.kodex

import hep.dataforge.context.Context
import hep.dataforge.data.Data
import hep.dataforge.data.NamedData
import hep.dataforge.goals.Goal
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MutableMetaNode
import hep.dataforge.values.NamedValue
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.future.await
import java.time.Instant
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Core DataForge classes extensions
 * Created by darksnake on 26-Apr-17.
 */

//Value operations

operator fun Value.plus(other: Value): Value {
    return when (this.type) {
        ValueType.NUMBER -> Value.of(this.numberValue() + other.numberValue());
        ValueType.STRING -> Value.of(this.stringValue() + other.stringValue());
        ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.timeValue().toEpochMilli() + other.timeValue().toEpochMilli()))
        ValueType.BOOLEAN -> Value.of(this.booleanValue() || other.booleanValue());
        ValueType.NULL -> other;
    }
}

operator fun Value.minus(other: Value): Value {
    return when (this.type) {
        ValueType.NUMBER -> Value.of(this.numberValue() - other.numberValue());
        ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.timeValue().toEpochMilli() - other.timeValue().toEpochMilli()))
        else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
    }
}

operator fun Value.times(other: Value): Value {
    return when (this.type) {
        ValueType.NUMBER -> Value.of(this.numberValue() * other.numberValue());
        else -> throw RuntimeException("Operation minus not allowed for ${this.type}");
    }
}

operator fun Value.plus(other: Any): Value {
    return this + Value.of(other);
}

operator fun Value.minus(other: Any): Value {
    return this - Value.of(other);
}

operator fun Value.times(other: Any): Value {
    return this * Value.of(other);
}

//Value comparison

operator fun Value.compareTo(other: Value): Int {
    return when (this.type) {
        ValueType.NUMBER -> this.numberValue().compareTo(other.numberValue());
        ValueType.STRING -> this.stringValue().compareTo(other.stringValue())
        ValueType.TIME -> this.timeValue().compareTo(other.timeValue())
        ValueType.BOOLEAN -> this.booleanValue().compareTo(other.booleanValue())
        ValueType.NULL ->
            if (other.isNull) {
                0
            } else {
                1
            }
    }
}

fun Value?.isNull(): Boolean {
    return this == null || this.isNull
}

//Meta operations

operator fun Meta.get(path: String): Value {
    return this.getValue(path);
}

operator fun <T : MutableMetaNode<*>> MutableMetaNode<T>.set(path: String, value: Value): T {
    return this.setValue(path, value);
}

operator fun <T : MutableMetaNode<*>> T.plusAssign(value: NamedValue) {
    this.setValue(value.name, value.anonymousValue);
}

operator fun <T : MutableMetaNode<*>> T.plusAssign(meta: Meta) {
    this.putNode(meta);
}

/**
 * Create a new meta with added node
 */
operator fun Meta.plus(meta: Meta): Meta {
    return this.builder.putNode(meta);
}

/**
 * create a new meta with added value
 */
operator fun Meta.plus(value: NamedValue): Meta {
    return this.builder.putValue(value.name, value.anonymousValue);
}

/**
 * Get a value if it is present and apply action to it
 */
fun Meta.useValue(valueName: String, action: (Value) -> Unit) {
    optValue(valueName).ifPresent(action)
}

/**
 * Get a meta node if it is present and apply action to it
 */
fun Meta.useMeta(metaName: String, action: (Meta) -> Unit) {
    optMeta(metaName).ifPresent(action)
}

/**
 * Perform some action on each meta element of the list if it is present
 */
fun Meta.useEachMeta(metaName: String, action: (Meta) -> Unit) {
    if(hasMeta(metaName)){
        getMetaList(metaName).forEach(action)
    }
}

/**
 * Get all meta nodes with the given name and apply action to them
 */
fun Meta.useMetaList(metaName: String, action: (List<Meta>) -> Unit) {
    if(hasMeta(metaName)){
        action(getMetaList(metaName))
    }
}

fun <T : Configurable> T.configure(transform: KMetaBuilder.() -> Unit): T {
    this.configure(hep.dataforge.kodex.buildMeta(this.config.name, transform));
    return this;
}

val Context.coroutineContext: CoroutineContext
    get() = optFeature(KodexPlugin::class.java).map { it.dispatcher }.orElse(CommonPool)

/**
 * Use goal as a suspending function
 */
suspend fun <R> Goal<R>.await(): R {
    if (this is Coal<R>) {
        //A special case for Coal
        return this.await();
    } else {
        return this.result().await();
    }
}

fun <T, R> Data<T>.pipe(type: Class<R>, dispatcher: CoroutineContext = CommonPool, transform: suspend (T) -> R): Data<R> {
    return Data(this.goal.pipe(dispatcher, transform), type, this.meta)
}

fun <T, R> NamedData<T>.pipe(type: Class<R>, dispatcher: CoroutineContext = CommonPool, transform: suspend (T) -> R): NamedData<R> {
    return NamedData(this.name, this.goal.pipe(dispatcher, transform), type, this.meta)
}
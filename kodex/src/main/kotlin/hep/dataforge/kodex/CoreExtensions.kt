package hep.dataforge.kodex

import hep.dataforge.context.Context
import hep.dataforge.data.Data
import hep.dataforge.data.NamedData
import hep.dataforge.goals.Goal
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaProvider
import hep.dataforge.meta.MutableMetaNode
import hep.dataforge.values.NamedValue
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import hep.dataforge.values.ValueType
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.future.await
import java.time.Instant
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Core DataForge classes extensions
 * Created by darksnake on 26-Apr-17.
 */

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

class ValueDelegate(private val valueName: String?) : ReadOnlyProperty<ValueProvider, Value?> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): Value? =
            thisRef.optValue(valueName ?: property.name).orElse(null)
}

class StringValueDelegate(private val valueName: String?) : ReadOnlyProperty<ValueProvider, String?> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): String? =
            thisRef.optString(valueName ?: property.name).orElse(null)
}

class BooleanValueDelegate(private val valueName: String?) : ReadOnlyProperty<ValueProvider, Boolean?> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): Boolean? =
            thisRef.optBoolean(valueName ?: property.name).orElse(null)
}

class TimeValueDelegate(private val valueName: String?) : ReadOnlyProperty<ValueProvider, Instant?> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): Instant? =
            thisRef.optTime(valueName ?: property.name).orElse(null)
}

class NumberValueDelegate(private val valueName: String?) : ReadOnlyProperty<ValueProvider, Number?> {
    override operator fun getValue(thisRef: ValueProvider, property: KProperty<*>): Number? =
            thisRef.optNumber(valueName ?: property.name).orElse(null)
}

/**
 * Delegate ValueProvider element to read only property
 */
fun ValueProvider.value(valueName: String? = null) = ValueDelegate(valueName)

fun ValueProvider.stringValue(valueName: String? = null) = StringValueDelegate(valueName)
fun ValueProvider.booleanValue(valueName: String? = null) = BooleanValueDelegate(valueName)
fun ValueProvider.timeValue(valueName: String? = null) = TimeValueDelegate(valueName)
fun ValueProvider.numberValue(valueName: String? = null) = NumberValueDelegate(valueName)


//Meta operations

operator fun Meta.get(path: String): Value = this.getValue(path)

operator fun <T : MutableMetaNode<*>> MutableMetaNode<T>.set(path: String, value: Value): T = this.setValue(path, value)

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

fun MetaProvider.meta(metaName: String? = null): MetaDelegate = MetaDelegate(metaName)

//Configuration extension

/**
 * Configure a configurable using in-place build meta
 */
fun <T : Configurable> T.configure(transform: KMetaBuilder.() -> Unit): T {
    this.configure(hep.dataforge.kodex.buildMeta(this.config.name, transform));
    return this;
}

class MutableValueDelegate(private val valueName: String?): ReadWriteProperty<MutableMetaNode<*>,Value?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): Value? =
            thisRef.optValue(valueName ?: property.name).orElse(null)

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: Value?) {
        thisRef.setValue(valueName ?: property.name, value);
    }
}

class MutableStringValueDelegate(private val valueName: String?): ReadWriteProperty<MutableMetaNode<*>,String?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): String? =
            thisRef.optString(valueName ?: property.name).orElse(null)

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: String?) {
        thisRef.setValue(valueName ?: property.name, value);
    }
}

class MutableBooleanValueDelegate(private val valueName: String?): ReadWriteProperty<MutableMetaNode<*>,Boolean?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): Boolean? =
            thisRef.optBoolean(valueName ?: property.name).orElse(null)

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: Boolean?) {
        thisRef.setValue(valueName ?: property.name, value);
    }
}

class MutableTimeValueDelegate(private val valueName: String?): ReadWriteProperty<MutableMetaNode<*>,Instant?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): Instant? =
            thisRef.optTime(valueName ?: property.name).orElse(null)

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: Instant?) {
        thisRef.setValue(valueName ?: property.name, value);
    }
}

class MutableNumberValueDelegate(private val valueName: String?): ReadWriteProperty<MutableMetaNode<*>,Number?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): Number? =
            thisRef.optNumber(valueName ?: property.name).orElse(null)

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: Number?) {
        thisRef.setValue(valueName ?: property.name, value);
    }
}

class MutableMetaDelegate(private val metaName: String?): ReadWriteProperty<MutableMetaNode<*>,Meta?> {
    override operator fun getValue(thisRef: MutableMetaNode<*>, property: KProperty<*>): Meta? =
            thisRef.optMeta(metaName ?: property.name).orElse(null);

    override operator fun setValue(thisRef: MutableMetaNode<*>, property: KProperty<*>, value: Meta?) {
        thisRef.setValue(metaName ?: property.name, value);
    }
}

/**
 * Delegate MutableMetaNode element to read/write property
 */
fun MutableMetaNode<*>.value(valueName: String? = null) = MutableValueDelegate(valueName)

fun MutableMetaNode<*>.stringValue(valueName: String? = null) = MutableStringValueDelegate(valueName)
fun MutableMetaNode<*>.booleanValue(valueName: String? = null) = MutableBooleanValueDelegate(valueName)
fun MutableMetaNode<*>.timeValue(valueName: String? = null) = MutableTimeValueDelegate(valueName)
fun MutableMetaNode<*>.numberValue(valueName: String? = null) = MutableNumberValueDelegate(valueName)
fun MutableMetaNode<*>.meta(metaName: String? = null) = MutableMetaDelegate(metaName)

//suspending functions

val Context.coroutineContext: CoroutineContext
    get() = optFeature(KodexPlugin::class.java).map { it.dispatcher }.orElse(CommonPool)

/**
 * Use goal as a suspending function
 */
suspend fun <R> Goal<R>.await(): R {
    return if (this is Coal<R>) {
        //A special case for Coal
        this.await();
    } else {
        this.result().await();
    }
}

fun <T, R> Data<T>.pipe(type: Class<R>, dispatcher: CoroutineContext = CommonPool, transform: suspend (T) -> R): Data<R> =
        Data(this.goal.pipe(dispatcher, transform), type, this.meta)

fun <T, R> NamedData<T>.pipe(type: Class<R>, dispatcher: CoroutineContext = CommonPool, transform: suspend (T) -> R): NamedData<R> =
        NamedData(this.name, this.goal.pipe(dispatcher, transform), type, this.meta)
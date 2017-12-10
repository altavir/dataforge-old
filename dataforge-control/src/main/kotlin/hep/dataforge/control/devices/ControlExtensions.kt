/*
 * Copyright  2017 Alexander Nozik.
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

package hep.dataforge.control.devices

import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import java.time.Instant
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StateDelegate(private val stateName: String?): ReadWriteProperty<Stateful, Value> {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Value =
            thisRef.getState(stateName ?: property.name)

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Value) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class StringStateDelegate(private val stateName: String?) : ReadWriteProperty<Stateful, String> {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): String =
            thisRef.getState(stateName ?: property.name).stringValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: String) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class BooleanStateDelegate(private val stateName: String?): ReadWriteProperty<Stateful, Boolean> {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Boolean =
            thisRef.getState(stateName ?: property.name).booleanValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Boolean) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class TimeStateDelegate(private val stateName: String?): ReadWriteProperty<Stateful, Instant>  {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Instant =
            thisRef.getState(stateName ?: property.name).timeValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Instant) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class NumberStateDelegate(private val stateName: String?): ReadWriteProperty<Stateful, Number>  {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Number =
            thisRef.getState(stateName ?: property.name).numberValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Number) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class DoubleStateDelegate(private val stateName: String?) : ReadWriteProperty<Stateful, Double> {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Double =
            thisRef.getState(stateName ?: property.name).doubleValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Double) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class IntStateDelegate(private val stateName: String?) : ReadWriteProperty<Stateful, Int> {
    operator override fun getValue(thisRef: Stateful, property: KProperty<*>): Int =
            thisRef.getState(stateName ?: property.name).intValue()

    operator override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Int) {
        thisRef.setState(stateName ?: property.name, value);
    }
}

class MetaStateDelegate(private val stateName: String?) : ReadWriteProperty<Stateful, Meta> {
    override fun getValue(thisRef: Stateful, property: KProperty<*>): Meta=
            thisRef.getMetaState(stateName ?: property.name)

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Meta) {
        thisRef.setMetaState(stateName ?: property.name, value);
    }

}


/**
 * Delegate states to read/write property
 */
fun Stateful.state(stateName: String? = null) = StateDelegate(stateName)
fun Stateful.stringState(stateName: String? = null) = StringStateDelegate(stateName)
fun Stateful.booleanState(stateName: String? = null) = BooleanStateDelegate(stateName)
fun Stateful.timeState(stateName: String? = null) = TimeStateDelegate(stateName)
fun Stateful.numberState(stateName: String? = null) = NumberStateDelegate(stateName)
fun Stateful.doubleState(stateName: String? = null) = DoubleStateDelegate(stateName)
fun Stateful.intState(stateName: String? = null) = IntStateDelegate(stateName)
fun Stateful.metaState(stateName: String? = null) = MetaStateDelegate(stateName)

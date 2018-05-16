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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description

import hep.dataforge.Named
import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.SimpleMetaMorph
import hep.dataforge.names.AnonymousNotAlowed
import hep.dataforge.values.Value
import hep.dataforge.values.ValueFactory
import hep.dataforge.values.ValueType
import java.util.*
import java.util.stream.Stream

/**
 * A descriptor for meta value
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
class ValueDescriptor(meta: Meta) : SimpleMetaMorph(meta), Named {

    /**
     * True if multiple values with this name are allowed.
     *
     * @return
     */
    val isMultiple: Boolean
        get() = meta.getBoolean("multiple", true)

    /**
     * True if the value is required
     *
     * @return
     */
    val isRequired: Boolean
        get() = meta.getBoolean("required", false)

    /**
     * Value name
     *
     * @return
     */
    override val name: String
        get() = meta.getString("name", "")

    /**
     * The value info
     *
     * @return
     */
    val info: String
        get() {
            return meta.getString("info", "")
        }

    /**
     * A list of allowed ValueTypes. Empty if any value type allowed
     *
     * @return
     */
    fun type(): List<ValueType> {
        return if (meta.hasValue("type")) {
            meta.getValue("type").list
                    .stream()
                    .map { v -> ValueType.valueOf(v.string) }
                    .toList()
        } else {
            emptyList()
        }
    }

    fun tags(): List<String> {
        return if (meta.hasValue("tags")) {
            Arrays.asList(*meta.getStringArray("tags"))
        } else {
            emptyList()
        }
    }

    /**
     * Check if given value is allowed for here. The type should be allowed and
     * if it is value should be within allowed values
     *
     * @param value
     * @return
     */
    fun isValueAllowed(value: Value): Boolean {
        return (type().isEmpty() || type().contains(ValueType.STRING) || type().contains(value.type)) && (allowedValues().isEmpty() || allowedValues().containsKey(value))
    }

    /**
     * Check if there is default for this value
     *
     * @return
     */
    fun hasDefault(): Boolean {
        return meta.hasValue("default")
    }

    /**
     * The default for this value. Null if there is no default.
     *
     * @return
     */
    fun defaultValue(): Value {
        return meta.getValue("default", ValueFactory.NULL)
    }

    /**
     * A list of allowed values with descriptions. If empty than any value is
     * allowed.
     *
     * @return
     */
    fun allowedValues(): Map<Value, String> {
        val map = HashMap<Value, String>()
        if (meta.hasMeta("allowedValue")) {
            for (allowed in meta.getMetaList("allowedValue")) {
                map[allowed.getValue("value")] = allowed.getString("description", "")
            }
        } else if (meta.hasValue("allowedValues")) {
            for (`val` in meta.getValue("allowedValues").list) {
                map[`val`] = ""
            }
        } else if (type().size == 1 && type()[0] === ValueType.BOOLEAN) {
            map[ValueFactory.of(true)] = ""
            map[ValueFactory.of(false)] = ""
        }

        return map
    }

    companion object {

        fun build(def: ValueDef): ValueDescriptor {
            val builder = MetaBuilder("value")
                    .setValue("name", def.key)
                    .setValue("type", def.type)
                    .setValue("tags", def.tags)

            if (!def.required) {
                builder.setValue("required", def.required)
            }

            if (!def.multiple) {
                builder.setValue("multiple", def.multiple)
            }

            if (!def.info.isEmpty()) {
                builder.setValue("info", def.info)
            }

            if (def.allowed.isNotEmpty()) {
                builder.setValue("allowedValues", def.allowed)
            } else if (def.enumeration != Any::class) {
                if (def.enumeration.java.isEnum) {
                    val values = def.enumeration.java.enumConstants
                    builder.setValue("allowedValues", Stream.of<Any>(*values).map<String> { it.toString() })
                } else {
                    throw RuntimeException("Only enumeration classes are allowed in 'enumeration' annotation property")
                }
            }


            if (!def.def.isEmpty()) {
                builder.setValue("default", def.def)
            }
            return ValueDescriptor(builder)
        }

        fun empty(valueName: String): ValueDescriptor {
            val builder = MetaBuilder("value")
                    .setValue("name", valueName)
            return ValueDescriptor(builder)
        }
    }
}

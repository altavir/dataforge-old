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

import hep.dataforge.kodex.set
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import org.slf4j.LoggerFactory

/**
 * Helper class to builder descriptors
 * @author Alexander Nozik
 */
class DescriptorBuilder(name: String = "node", override val meta: Configuration = Configuration("node")) : Metoid {
    //var name by meta.mutableStringValue()
    var required by meta.mutableBooleanValue()
    var multiple by meta.mutableBooleanValue()
    var default by meta.mutableNode()
    var info by meta.mutableStringValue()
    var tags: List<String> by meta.customMutableValue(read = { it.list.map { it.string } }, write = { Value.of(it) })

    init {
        meta["name"] = name
    }

    //TODO add caching for node names?
    /**
     * Check if this node condtains descriptor node with given name
     */
    private fun hasNodeDescriptor(name: String): Boolean {
        return meta.getMetaList("node").find { it.getString("name") == name } != null
    }

    /**
     * Check if this node contains value with given name
     */
    private fun hasValueDescriptor(name: String): Boolean {
        return meta.getMetaList("value").find { it.getString("name") == name } != null
    }

    /**
     * append child descriptor builder
     */
    fun node(childDescriptor: NodeDescriptor): DescriptorBuilder {
        if (!hasNodeDescriptor(childDescriptor.name)) {
            meta.putNode(childDescriptor.meta)
        } else {
            LoggerFactory.getLogger(javaClass).warn("Trying to replace existing node descriptor ${childDescriptor.name}")
        }
        return this
    }

    /**
     * Append node to this descriptor respecting the path
     */
    fun node(name: Name, childBuilder: DescriptorBuilder.() -> Unit): DescriptorBuilder {
        val parent = if (name.length == 1) {
            this
        } else {
            buildChild(name.cutLast())
        }
        parent.node(DescriptorBuilder(name.last.toString()).apply(childBuilder).build())
        return this
    }

    /**
     * Add a node using DSL builder. Name could be non-atomic
     */
    fun node(name: String, childBuilder: DescriptorBuilder.() -> Unit): DescriptorBuilder {
        return node(Name.of(name), childBuilder)
    }

    /**
     * Build a descrptor builder for child node. Changes in child descriptor reflect on this descriptor builder
     */
    private fun buildChild(name: Name): DescriptorBuilder {
        return when (name.length) {
            0 -> this
            1 -> {
                val node = meta.getMetaList("node").find { it.getString("name") == name.first.toUnescaped() }
                        ?: Configuration("node").also { this.meta.attachNode(it) }
                DescriptorBuilder(name.first.toUnescaped(), node)
            }
            else -> {
                buildChild(name.first).buildChild(name.cutFirst())
            }
        }
    }

    /**
     * Add node from annotation
     */
    fun node(nodeDef: NodeDef): DescriptorBuilder {
        return node(nodeDef.key) {
            info = nodeDef.info
            required = nodeDef.required
            multiple = nodeDef.multiple
            tags = nodeDef.tags.asList()
            Descriptors.forDef(nodeDef)?.let { update(it) }
        }
    }

    /**
     * Add a value respecting its path inside descriptor
     */
    fun value(descriptor: ValueDescriptor): DescriptorBuilder {
        val name = Name.of(descriptor.name)
        val parent = if (name.length == 1) {
            this
        } else {
            buildChild(name.cutLast())
        }

        if (!parent.hasValueDescriptor(name.last.toUnescaped())) {
            parent.meta.putNode(descriptor.toMeta().builder.apply { this["name"] = name.last.toUnescaped() })
        } else {
            LoggerFactory.getLogger(javaClass).warn("Trying to replace existing value descriptor ${descriptor.name}")
        }
        return this
    }

    fun value(def: ValueDef): DescriptorBuilder {
        return value(ValueDescriptor.build(def))
    }

    /**
     * Create value descriptor from its fields. Name could be non-atomic
     */
    fun value(
            name: String,
            info: String = "",
            defaultValue: Any? = null,
            required: Boolean = false,
            multiple: Boolean = false,
            types: List<ValueType> = emptyList(),
            allowedValues: List<Any> = emptyList()
    ): DescriptorBuilder {
        return value(ValueDescriptor.build(name, info, defaultValue, required, multiple, types, allowedValues))
    }

    fun update(descriptor: NodeDescriptor): DescriptorBuilder {
        //TODO update primary fields
        descriptor.valueDescriptors().forEach {
            this.value(it.value)
        }
        descriptor.childrenDescriptors().forEach {
            this.node(it.value)
        }
        return this
    }

    fun build(): NodeDescriptor {
        return NodeDescriptor(meta)
    }

}

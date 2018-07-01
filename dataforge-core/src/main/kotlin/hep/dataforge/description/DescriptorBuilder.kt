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

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import org.slf4j.LoggerFactory

/**
 * Helper class to builder descriptors
 * @author Alexander Nozik
 */
class DescriptorBuilder(override val meta: MetaBuilder = MetaBuilder("node")) : Metoid {
    var name by meta.mutableStringValue()
    var required by meta.mutableBooleanValue()
    var multiple by meta.mutableBooleanValue()
    var default by meta.mutableNode()
    var info by meta.mutableStringValue()
    var tags: List<String> by meta.mutableCustomValue(read = { it.list.map { it.string } }, write = { Value.of(it) })

    //TODO add caching for node names?
    private fun hasNodeDescriptor(name: String): Boolean {
        return meta.getMetaList("node").find { it.getString("name") == name } != null
    }

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
            LoggerFactory.getLogger(javaClass).warn("Trying to replace existing node descriptor")
        }
        return this
    }

    /**
     * Append node to this descriptor respecting the path
     */
    fun node(name: Name, childBuilder: DescriptorBuilder.() -> Unit): DescriptorBuilder {
        val root = if (name.length == 1) {
            this
        } else {
            buildChild(name.cutLast())
        }
        root.node(DescriptorBuilder().apply(childBuilder).apply { this.name = name.last.toString() }.build())
        return this
    }

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
                DescriptorBuilder(
                        meta.getMetaList("node")
                                .find {
                                    it.getString("name") == name.first.toString()
                                }
                                ?: MetaBuilder("node").apply {
                                    setValue("name", name)
                                    meta.attachNode(this)
                                }
                )
            }
            else -> {
                buildChild(name.first).buildChild(name.cutFirst())
            }
        }
    }


//    fun node(path: String, descriptor: NodeDescriptor): DescriptorBuilder {
//        meta.putNode(Name.joinString(path, descriptor.name), descriptor.meta)
//        return this
//    }
//
//    infix fun String.to(builder: DescriptorBuilder.() -> Unit) {
//        node(this, DescriptorBuilder().also { it.name = this }.apply(builder).build())
//    }

//    fun node(childType: AnnotatedElement): DescriptorBuilder {
//        return node(Descriptors.buildDescriptor(childType))
//    }

    fun node(nodeDef: NodeDef): DescriptorBuilder {
        return node(nodeDef.key) {
            info = nodeDef.info
            required = nodeDef.required
            multiple = nodeDef.multiple
            tags = nodeDef.tags.asList()
        }
    }

//    fun node(path: String, childType: AnnotatedElement): DescriptorBuilder {
//        return node(path, Descriptors.buildDescriptor(childType))
//    }

    fun value(descriptor: ValueDescriptor): DescriptorBuilder {
        if (!hasValueDescriptor(descriptor.name)) {
            meta.putNode(descriptor.toMeta())
        } else {
            LoggerFactory.getLogger(javaClass).warn("Trying to replace existing value descriptor")
        }
        return this
    }

//    fun value(path: String, descriptor: ValueDescriptor): DescriptorBuilder {
//        meta.putNode(Name.joinString(path, descriptor.name), descriptor.toMeta())
//        return this
//    }

    fun value(
            name: String,
            info: String = "",
            defaultValue: Any? = null,
            required: Boolean = false,
            multiple: Boolean = false,
            types: List<ValueType> = emptyList(),
            allowedValues: List<Any>? = null
    ): DescriptorBuilder {
        val valueBuilder = MetaBuilder("value")
                .setValue("name", name)
                .setValue("type", types)
                .setValue("required", required)
                .setValue("multiple", multiple)
                .setValue("info", info)
                .setValue("default", defaultValue)
                .setValue("allowedValues", allowedValues)
        meta.putNode(valueBuilder)
        return this
    }

    /**
     * Update this builder from external descriptor. Elements of this descriptor take precedence
     */
    fun update(descriptor: NodeDescriptor) {

    }

    fun build(): NodeDescriptor {
        return NodeDescriptor(meta.build())
    }

//    /**
//     * Put a node or value description inside existing meta builder creating intermediate nodes
//     *
//     * @param builder
//     * @param meta
//     */
//    private fun putDescription(builder: MetaBuilder, meta: Meta) {
//        var nodeName = Name.of(meta.getString("name"))
//        var currentNode = builder
//        while (nodeName.length > 1) {
//            val childName = nodeName.first.toString()
//            val finalCurrentNode = currentNode
//            currentNode = finalCurrentNode.getMetaList("node").stream()
//                    .filter { node -> node.getString("name") == childName }
//                    .findFirst()
//                    .orElseGet {
//                        val newChild = MetaBuilder("node").setValue("name", childName)
//                        finalCurrentNode.attachNode(newChild)
//                        newChild
//                    }
//            nodeName = nodeName.cutFirst()
//        }
//
//        val childName = nodeName.toString()
//        val finalCurrentNode = currentNode
//        currentNode.getMetaList(meta.name).stream()
//                .filter { node -> node.getString("name") == childName }
//                .findFirst()
//                .orElseGet {
//                    val newChild = MetaBuilder(meta.name).setValue("name", childName)
//                    finalCurrentNode.attachNode(newChild)
//                    newChild
//                }.update(meta).setValue("name", childName)
//    }

}

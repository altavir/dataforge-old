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
import hep.dataforge.isAnonymous
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaMorph
import hep.dataforge.meta.Metoid
import hep.dataforge.names.Name
import java.util.*

/**
 * Descriptor for meta node. Could contain additional information for viewing
 * and editing.
 *
 * @author Alexander Nozik
 */
open class NodeDescriptor(override val meta: Meta) : Named, MetaMorph, Metoid {

    /**
     * True if multiple children with this nodes name are allowed. Anonimous
     * nodes are always single
     *
     * @return
     */
    val isMultiple: Boolean
        get() = meta.getBoolean("multiple", true) || this.isAnonymous

    /**
     * True if the node is required
     *
     * @return
     */
    val isRequired: Boolean
        get() = meta.getBoolean("required", false)

    /**
     * The node description
     *
     * @return
     */
    open val info: String
        get() = meta.getString("info", "")

    /**
     * A list of tags for this node. Tags used to customize node usage
     *
     * @return
     */
    val tags: List<String>
        get() = if (meta.hasValue("tags")) {
            Arrays.asList(*meta.getStringArray("tags"))
        } else {
            emptyList()
        }

    /**
     * The name of this node
     *
     * @return
     */
    override val name: String
        get() = meta.getString("name", meta.name)

    /**
     * The list of value descriptors
     *
     * @return
     */
    fun valueDescriptors(): Map<String, ValueDescriptor> {
        val map = HashMap<String, ValueDescriptor>()
        if (meta.hasMeta("value")) {
            for (valueNode in meta.getMetaList("value")) {
                val vd = ValueDescriptor(valueNode)
                map[vd.name] = vd
            }
        }
        return map
    }

    /**
     * The child node descriptor for given name. Name syntax is supported.
     *
     * @param name
     * @return
     */
    fun optChildDescriptor(name: String): Optional<NodeDescriptor> {
        return optChildDescriptor(Name.of(name))
    }

    fun optChildDescriptor(name: Name): Optional<NodeDescriptor> {
        return if (name.length == 1) {
            Optional.ofNullable(childrenDescriptors()[name.toUnescaped()])
        } else {
            optChildDescriptor(name.cutLast()).flatMap { it -> it.optChildDescriptor(name.last) }
        }
    }

    /**
     * The value descriptor for given value name. Name syntax is supported.
     *
     * @param name
     * @return
     */
    fun optValueDescriptor(name: String): Optional<ValueDescriptor> {
        return optValueDescriptor(Name.of(name))
    }

    fun optValueDescriptor(name: Name): Optional<ValueDescriptor> {
        return if (name.length == 1) {
            Optional.ofNullable(valueDescriptors()[name.toUnescaped()])
        } else {
            optChildDescriptor(name.cutLast()).flatMap { it -> it.optValueDescriptor(name.last) }
        }
    }

    /**
     * The map of children node descriptors
     *
     * @return
     */
    fun childrenDescriptors(): Map<String, NodeDescriptor> {
        val map = HashMap<String, NodeDescriptor>()
        if (meta.hasMeta("node")) {
            for (node in meta.getMetaList("node")) {
                val nd = NodeDescriptor(node)
                map[nd.name] = nd
            }
        }
        return map
    }

    /**
     * Check if this node has default
     *
     * @return
     */
    fun hasDefault(): Boolean {
        return meta.hasMeta("default")
    }

    /**
     * The default meta for this node (could be multiple). Null if not defined
     *
     * @return
     */
    fun defaultNode(): List<Meta>? {
        return if (meta.hasMeta("default")) {
            meta.getMetaList("default")
        } else {
            null
        }
    }

    /**
     * Identify if this descriptor has child value descriptor with default
     *
     * @param name
     * @return
     */
    fun hasDefaultForValue(name: String): Boolean {
        return optValueDescriptor(name).map<Boolean> { it.hasDefault() }.orElse(false)
    }

    /**
     * The key of the value which is used to display this node in case it is
     * multiple. By default, the key is empty which means that node index is
     * used.
     *
     * @return
     */
    fun titleKey(): String {
        return meta.getString("titleKey", "")
    }

    override fun toMeta(): Meta {
        return meta
    }

    companion object {

        fun build(nodeDef: NodeDef): NodeDescriptor {
            return NodeDescriptor(Descriptors.buildDescriptorMeta(nodeDef))
        }

        fun empty(nodeName: String): NodeDescriptor {
            return NodeDescriptor(Meta.buildEmpty(nodeName))
        }
    }
}

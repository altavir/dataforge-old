package hep.dataforge.fx.configuration

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding
import java.util.*
import kotlin.collections.ArrayList

/**
 * Tree item for node
 * Created by darksnake on 30-Apr-17.
 */
class ConfigFXNode(
        name: String,
        parent: ConfigFXNode? = null,
        rootConfig: Configuration? = null,
        rootDescriptor: NodeDescriptor? = null) : ConfigFX(name, parent) {

    constructor(config: Configuration, desc: NodeDescriptor?) : this(config.name, null, config, desc);

    val descriptor: NodeDescriptor? = rootDescriptor ?: parent?.descriptor?.childDescriptor(name);

    override val contentProperty: ObjectBinding<Configuration?> = object : ObjectBinding<Configuration?>() {
        init {
            parent?.let { bind(it.contentProperty) }
        }

        override fun computeValue(): Configuration? {
            return rootConfig ?: parent?.configuration?.getMeta(name);
        }
    }

    val configuration: Configuration?
        get() = contentProperty.get()

    //override fun getDescription():String = {descriptor?.info() ?: ""}

    override fun getDescription(): String {
        return descriptor?.info() ?: ""
    }


    /**
     * Get existing configuration node or create and attach new one
     *
     * @return
     */
    private fun getOrBuildNode(): Configuration {
        return configuration ?: if (parent == null) {
            throw RuntimeException("The configuration for root node is note defined")
        } else {
            val cfg = Configuration(name)
            parent.getOrBuildNode().attachNode(cfg)
            invalidate()
            cfg
        }
    }

    /**
     * return children;
     */
    override fun getChildren(): List<ConfigFX> {
        val list = ArrayList<ConfigFX>()
        val nodeNames = HashSet<String>()
        val valueNames = HashSet<String>()
        configuration?.let { config ->
            config.nodeNames.forEach { childNodeName ->
                nodeNames.add(childNodeName)
                val nodeSize = config.getMetaList(childNodeName).size
                if (nodeSize == 1) {
                    list.add(ConfigFXNode(childNodeName, this))
                } else {
                    (0 until nodeSize).mapTo(list) { ConfigFXNode("$childNodeName[$it]", this) }
                }
            }
            //    Adding all existing values and nodes
            config.valueNames.forEach { childValueName ->
                valueNames.add(childValueName)
                list.add(ConfigFXValue(childValueName, this))
            }
        }
        //    adding nodes and values from descriptor
        descriptor?.let { desc ->
            desc.childrenDescriptors().keys.forEach { nodeName ->
                //Adding only those nodes, that have no configuration of themselves
                if (!nodeNames.contains(nodeName)) {
                    list.add(ConfigFXNode(nodeName, this))
                }
            }
            desc.valueDescriptors().keys.forEach { valueName ->
                //    Adding only those values, that have no configuration of themselves
                if (!valueNames.contains(valueName)) {
                    list.add(ConfigFXValue(valueName, this))
                }
            }
        }
        return list
    }

    fun setValue(name: String, value: Value) {
        getOrBuildNode().setValue(name, value)
        invalidate()
    }

    fun addNode(name: String) {
        getOrBuildNode().putNode(name, Meta.empty())
        invalidate()
    }

    override fun remove() {
        configuration?.let {
            parent?.configuration?.removeNode(name);
            parent?.invalidate()
        }
    }

    fun removeValue(valueName: String) {
        configuration?.removeValue(valueName)
    }

    private fun invalidate() {
        contentProperty.invalidate()
    }
}

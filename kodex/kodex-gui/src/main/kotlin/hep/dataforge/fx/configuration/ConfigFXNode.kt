package hep.dataforge.fx.configuration

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.util.*

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

    val configProperty: ObjectBinding<Configuration?> = object : ObjectBinding<Configuration?>() {
        init {
            parent?.let { bind(it.configProperty) }
        }

        override fun computeValue(): Configuration? {
            return rootConfig ?: parent?.configuration?.getMeta(name);
        }
    }

    val configuration: Configuration?
        get() = configProperty.get()

    override val description = descriptor?.info() ?: ""

//    val configurationPresent = Bindings.isNotNull(configurationProperty)
//
//    val descriptorPresent = Bindings.isNotNull(descriptorProperty)


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
    override fun getChildren(): ObservableList<ConfigFX> {
        val list = FXCollections.observableArrayList<ConfigFX>()
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

    fun addValue(name: String, value: Value) {
        getOrBuildNode().putValue(name, value)
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

    override fun invalidate() {
        super.invalidate()
        configProperty.invalidate()
    }
}

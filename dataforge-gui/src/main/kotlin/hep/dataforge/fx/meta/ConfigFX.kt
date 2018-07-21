package hep.dataforge.fx.meta

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.description.ValueDescriptor
import hep.dataforge.fx.values.ValueCallbackResponse
import hep.dataforge.fx.values.ValueChooserFactory
import hep.dataforge.isNull
import hep.dataforge.meta.ConfigChangeListener
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.util.HashSet
import kotlin.collections.ArrayList

/**
 * A node, containing relative representation of configuration node and description
 * Created by darksnake on 01-May-17.
 */
sealed class ConfigFX(val name: String, val parent: ConfigFXNode? = null) {
    /**
     * remove itself from parent
     */
    abstract fun remove()

    abstract val isEmpty: ObservableBooleanValue
    abstract val descriptionProperty: ObservableStringValue

    val nameProperty = object : StringBinding() {
        override fun computeValue(): String {
            return name
        }
    }

    abstract fun invalidate()

    abstract fun invalidateValue(path: Name)

    abstract fun invalidateNode(path: Name)
}


/**
 * Tree item for node
 * Created by darksnake on 30-Apr-17.
 */
open class ConfigFXNode(
        name: String,
        parent: ConfigFXNode? = null) : ConfigFX(name, parent) {

    open val descriptor: NodeDescriptor? = parent?.descriptor?.getNodeDescriptor(name);

    open val configProperty: ObjectBinding<Configuration?> = object : ObjectBinding<Configuration?>() {
        init {
            parent?.let { bind(it.configProperty) }
        }

        override fun computeValue(): Configuration? {
            return parent?.configuration?.optMeta(name)?.orElse(null) as Configuration?;
        }
    }

    val configuration: Configuration?
        get() = configProperty.get()


    override val descriptionProperty: ObservableStringValue = object : StringBinding() {
        override fun computeValue(): String {
            return descriptor?.info ?: ""
        }
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
            Configuration(name).also {
                parent.getOrBuildNode().attachNode(it)
            }
        }
    }

    override val isEmpty: ObservableBooleanValue
        get() = configProperty.booleanBinding { it == null }

    val children: ObservableList<ConfigFX> by lazy {
        FXCollections.observableArrayList<ConfigFX>().apply {
            setAll(buildChildren())
        }
    }

    private fun updateChildren() {
        children.setAll(buildChildren())
    }

    private fun buildChildren(): List<ConfigFX> {
        val list: MutableList<ConfigFX> = ArrayList();
        val nodeNames = HashSet<String>()
        val valueNames = HashSet<String>()
        configuration?.let { config ->
            config.nodeNames.forEach { childNodeName ->
                nodeNames.add(childNodeName)
                val nodeSize = config.getMetaList(childNodeName).size
                if (nodeSize == 1) {
                    list.add(ConfigFXNode(childNodeName, this@ConfigFXNode))
                } else {
                    (0 until nodeSize).mapTo(list) { ConfigFXNode("$childNodeName[$it]", this@ConfigFXNode) }
                }
            }
            //    Adding all existing values and nodes
            config.valueNames.forEach { childValueName ->
                valueNames.add(childValueName)
                list.add(ConfigFXValue(childValueName, this@ConfigFXNode))
            }
        }
        //    adding nodes and values from descriptor
        descriptor?.let { desc ->
            desc.childrenDescriptors().keys.forEach { nodeName ->
                //Adding only those nodes, that have no configuration of themselves
                if (!nodeNames.contains(nodeName)) {
                    list.add(ConfigFXNode(nodeName, this@ConfigFXNode))
                }
            }
            desc.valueDescriptors().keys.forEach { valueName ->
                //    Adding only those values, that have no configuration of themselves
                if (!valueNames.contains(valueName)) {
                    list.add(ConfigFXValue(valueName, this@ConfigFXNode))
                }
            }
        }
        return list;
    }

    fun addValue(name: String) {
        getOrBuildNode().setValue(name, "not defined")
    }

    fun setValue(name: String, value: Value) {
        getOrBuildNode().setValue(name, value)
    }

    fun removeValue(valueName: String) {
        configuration?.removeValue(valueName)
    }

    fun addNode(name: String) {
        getOrBuildNode().requestNode(name)
    }

    fun removeNode(name: String) {
        configuration?.removeNode(name)
    }

    override fun remove() {
        configuration?.let {
            parent?.removeNode(name);
        }
    }

    override fun invalidate() {
        configProperty.invalidate()
        updateChildren()
    }

    override fun invalidateValue(path: Name) {
        if (path.length == 1) {
            children.find { it is ConfigFXValue && it.name == path.first.toString() }?.invalidate()
        } else if (path.length > 1) {
            children.find { it is ConfigFXNode && it.name == path.first.toString() }?.invalidateValue(path.cutFirst())
        }
    }

    override fun invalidateNode(path: Name) {
        when {
            path.isEmpty() -> invalidate()
            path.length == 1 -> children.find { it is ConfigFXNode && it.name == path.first.toString() }?.invalidate()
            else -> children.find { it is ConfigFXNode && it.name == path.first.toString() }?.invalidateNode(path.cutFirst())
        }
    }
}

class ConfigFXRoot(rootConfig: Configuration, rootDescriptor: NodeDescriptor? = null) : ConfigFXNode(rootConfig.name), ConfigChangeListener {

    override val descriptor: NodeDescriptor? = rootDescriptor;

    override val configProperty: ObjectBinding<Configuration?> = object : ObjectBinding<Configuration?>() {
        override fun computeValue(): Configuration? {
            return rootConfig
        }
    }

    init {
        rootConfig.addObserver(this)
    }

    override fun notifyValueChanged(valueName: Name, oldItem: Value?, newItem: Value?) {
        if (oldItem.isNull() || newItem.isNull()) {
            invalidateNode(valueName.cutLast())
        } else {
            invalidateValue(valueName)
        }
    }

    override fun notifyNodeChanged(nodeName: Name, oldItem: MutableList<out Meta>, newItem: MutableList<out Meta>) {
        if (oldItem.isEmpty() || newItem.isEmpty()) {
            invalidateNode(nodeName.cutLast())
        } else {
            invalidateNode(nodeName)
        }
    }
}


/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name, parent) {

    val descriptor: ValueDescriptor? = parent.descriptor?.getValueDescriptor(name);

    override val descriptionProperty: ObservableStringValue = object : StringBinding() {
        override fun computeValue(): String {
            return descriptor?.info ?: ""
        }
    }

    private val valueProperty = object : ObjectBinding<Value?>() {
        init {
            bind(parent.configProperty)
        }

        override fun computeValue(): Value? {
            return parent.configuration?.optValue(name)?.orElse(descriptor?.default)
        }
    }

    override val isEmpty: ObservableBooleanValue = parent.configProperty.booleanBinding(valueProperty) {
        !(it?.hasValue(name) ?: false)
    }


    var value: Value
        set(value) {
            parent?.setValue(name, value)
        }
        get() = valueProperty.get() ?: Value.NULL


    override fun remove() {
        parent?.removeValue(name)
    }

    override fun invalidate() {
        valueProperty.invalidate()
        valueChooser.setDisplayValue(value)
    }

    override fun invalidateValue(path: Name) {
        if (path.isEmpty()) {
            invalidate()
        }
    }

    override fun invalidateNode(path: Name) {
        //do nothing
    }

    val valueChooser = ValueChooserFactory.build(value, descriptor) { value: Value ->
        this.value = value
        ValueCallbackResponse(true, value, "")
    }


}

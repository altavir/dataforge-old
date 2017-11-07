package hep.dataforge.fx.configuration

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.meta.Configuration
import javafx.event.Event
import javafx.scene.Node
import javafx.scene.control.TreeItem

import java.util.function.Function
import java.util.stream.Collectors

/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXTreeItem(value: ConfigFX, graphic: Node? = null) : TreeItem<ConfigFX>(value, graphic) {

    private val descriptorProvider: Function<Configuration, NodeDescriptor>? = null


    /**
     * Is this branch a root
     *
     * @return
     */
    val isRoot: Boolean
        get() = this.parent == null

    init {
        fillChildren()
        value.addObserver { o, arg -> invalidate() }
    }


    private fun fillChildren() {
        children.setAll(
                value.getChildren().stream()
                        .filter { cfg -> toShow(cfg) }
                        .map { ConfigFXTreeItem(it) }
                        .collect(Collectors.toList())
        )
    }

    override fun isLeaf(): Boolean {
        return value is ConfigFXValue
    }


    private fun toShow(cfg: ConfigFX): Boolean {
        return when (cfg) {
            is ConfigFXNode -> cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false
            is ConfigFXValue -> cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false
            else -> true
        }
    }

    private fun invalidate() {
        fillChildren()
        val event = TreeItem.TreeModificationEvent(TreeItem.valueChangedEvent<Any>(), this)
        Event.fireEvent(this, event)
    }

    companion object {
        /**
         * The tag not to display node or value in configurator
         */
        val NO_CONFIGURATOR_TAG = "nocfg"
    }

}

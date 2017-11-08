/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.meta.Configuration
import hep.dataforge.values.Value
import javafx.beans.Observable
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Text
import tornadofx.*

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
class ConfigEditor(val configuration: Configuration, val descriptor: NodeDescriptor? = null) : Fragment() {

    companion object {
        /**
         * The tag not to display node or value in configurator
         */
        val NO_CONFIGURATOR_TAG = "nocfg"
    }

    val filter: (ConfigFX) -> Boolean = { cfg ->
        when (cfg) {
            is ConfigFXNode -> !(cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false)
            is ConfigFXValue -> !(cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false)
            else -> true
        }
    }

    override val root = borderpane {
        center = treetableview<ConfigFX> {
            root = ConfigFXTreeItem(ConfigFXNode(configuration, descriptor))
            root.isExpanded = true
            sortMode = TreeSortMode.ALL_DESCENDANTS
            columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
            column("Name") { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.nameProperty }
                    .setCellFactory { param: TreeTableColumn<ConfigFX, String> ->
                        object : TextFieldTreeTableCell<ConfigFX, String>() {
                            override fun updateItem(item: String?, empty: Boolean) {
                                super.updateItem(item, empty)
                                if (!empty) {
                                    setNameColor()
                                    if (treeTableRow.item != null) {
                                        contextmenu {
                                            item("Remove") {
                                                action {
                                                    treeTableRow.item.remove()
                                                }
                                            }
                                        }
                                        treeTableRow.item.isEmpty.addListener { _: Observable -> setNameColor() }
                                    }
                                } else {
                                    contextMenu = null
                                }
                            }

                            private fun setNameColor() {
                                if (treeTableRow.item != null) {
                                    textFill = if (!treeTableRow.item.isEmpty.get()) {
                                        Color.BLACK
                                    } else {
                                        Color.GRAY
                                    }
                                }
                            }
                        }
                    }

            column("Value") { param: TreeTableColumn.CellDataFeatures<ConfigFX, ConfigFX> ->
                param.value.valueProperty()
            }.setCellFactory {
                ValueCell()
            }

            column("Description") { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.descriptionProperty }
                    .setCellFactory { param: TreeTableColumn<ConfigFX, String> ->
                        val cell = TreeTableCell<ConfigFX, String>()
                        val text = Text()
                        cell.graphic = text
                        cell.prefHeight = Control.USE_COMPUTED_SIZE
                        text.wrappingWidthProperty().bind(param.widthProperty())
                        text.textProperty().bind(cell.itemProperty())
                        cell
                    }
        }
    }

    private fun showNameDialog(forNode: Boolean): String? {
        val dialog = TextInputDialog()
        if (forNode) {
            dialog.title = "Node name selection"
            dialog.contentText = "Enter a name for new node: "
        } else {
            dialog.title = "Value name selection"
            dialog.contentText = "Enter a name for new value: "
        }
        dialog.headerText = null

        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    private inner class ValueCell : TreeTableCell<ConfigFX, ConfigFX?>() {

        public override fun updateItem(item: ConfigFX?, empty: Boolean) {
            if (!empty) {
                //val row = treeTableRow.item
                if (item != null) {
                    when (item) {
                        is ConfigFXValue -> {
                            text = null
                            val chooser = item.valueChooser
                            graphic = chooser.node
                        }
                        is ConfigFXNode -> {
                            text = null
                            graphic = hbox {
                                button("+Node") {
                                    hgrow = Priority.ALWAYS
                                    action {
                                        showNameDialog(true)?.let {
                                            item.addNode(it)
                                        }
                                    }
                                }
                                button("+Value") {
                                    hgrow = Priority.ALWAYS
                                    action {
                                        showNameDialog(false)?.let { item.setValue(it, Value.NULL) }
                                    }
                                }
                            }
                        }
                        else -> {
                            text = null
                            graphic = null
                            isEditable = false
                        }
                    }

                }
            } else {
                text = null
                graphic = null
            }
        }

    }

    class ConfigFXTreeItem(value: ConfigFX, graphic: Node? = null, val filter: (ConfigFX) -> Boolean = { true }) : TreeItem<ConfigFX>(value, graphic) {

        //private val descriptorProvider: Function<Configuration, NodeDescriptor>? = null

        init {
            (value as?ConfigFXNode)?.let { node ->
                fillChildren(node)
                node.children.onChange {
                    fillChildren(node)
                }
            }
        }


        private fun fillChildren(node: ConfigFXNode) {
            children.setAll(node.children.filter(filter).map { ConfigFXTreeItem(it) })
        }

        override fun isLeaf(): Boolean {
            return value is ConfigFXValue
        }
    }
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration

import hep.dataforge.description.NodeDescriptor
import hep.dataforge.meta.Configuration
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.controlsfx.glyphfont.Glyph
import tornadofx.*

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
class ConfigEditor(val configuration: Configuration, val descriptor: NodeDescriptor? = null) : Fragment() {

    val filter: (ConfigFX) -> Boolean = { cfg ->
        when (cfg) {
            is ConfigFXNode -> !(cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false)
            is ConfigFXValue -> !(cfg.descriptor?.tags()?.contains(NO_CONFIGURATOR_TAG) ?: false)
            else -> true
        }
    }

    private fun TreeItem<ConfigFX>.update(): TreeItem<ConfigFX> {
        (value as? ConfigFXNode)?.let {
            children.setAll(it.children.filter(filter).map { TreeItem(it).update() })
            it.children.onChange {
                update()
            }
        }
        return this
    }

    override val root = borderpane {
        center = treetableview<ConfigFX> {
            root = TreeItem(ConfigFXRoot(configuration, descriptor))
            root.update()
            root.isExpanded = true
            sortMode = TreeSortMode.ALL_DESCENDANTS
            columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
            column("Name") { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.nameProperty }
                    .setCellFactory {
                        object : TextFieldTreeTableCell<ConfigFX, String>() {
                            override fun updateItem(item: String?, empty: Boolean) {
                                super.updateItem(item, empty)
                                if (!empty) {
                                    if (treeTableRow.item != null) {
                                        textFillProperty().bind(treeTableRow.item.isEmpty.objectBinding {
                                            if (it!!) {
                                                Color.GRAY
                                            } else {
                                                Color.BLACK
                                            }
                                        })
                                        contextmenu {
                                            item("Remove") {
                                                action {
                                                    treeTableRow.item.remove()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    contextMenu = null
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

    private fun showNodeDialog(): String? {
        val dialog = TextInputDialog()
        dialog.title = "Node name selection"
        dialog.contentText = "Enter a name for new node: "
        dialog.headerText = null

        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    private fun showValueDialog(): String? {
        val dialog = TextInputDialog()
        dialog.title = "Value name selection"
        dialog.contentText = "Enter a name for new value: "
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
                                button("", Glyph("FontAwesome", "PLUS_CIRCLE")) {
                                    action {
                                        showNodeDialog()?.let {
                                            item.addNode(it)
                                        }
                                    }
                                }
                                pane {
                                    hgrow = Priority.ALWAYS
                                }
                                button("", Glyph("FontAwesome", "PLUS_SQUARE")) {
                                    action {
                                        showValueDialog()?.let {
                                            item.addValue(it)
                                        }
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

    companion object {
        /**
         * The tag not to display node or value in configurator
         */
        const val NO_CONFIGURATOR_TAG = "nocfg"
    }
}



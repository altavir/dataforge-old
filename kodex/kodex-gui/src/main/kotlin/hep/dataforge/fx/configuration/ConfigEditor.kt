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
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.paint.Color
import javafx.scene.text.Text
import tornadofx.*

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
class ConfigEditor(val configuration: Configuration, val descriptor: NodeDescriptor? = null) : Fragment() {

    override val root = borderpane {
        treetableview<ConfigFX> {
            root = ConfigFXTreeItem(ConfigFXNode(configuration, descriptor))
            root.isExpanded = true
            sortMode = TreeSortMode.ALL_DESCENDANTS
            column("Name") { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.nameProperty }
                    .setCellFactory { param: TreeTableColumn<ConfigFX, String> ->
                        object : TextFieldTreeTableCell<ConfigFX, String>() {
                            override fun updateItem(item: String, empty: Boolean) {
                                super.updateItem(item, empty)
                                setNameColor()
                                if (treeTableRow.item != null) {
                                    treeTableRow.item.contentProperty.isNotNull.addListener { _: Observable -> setNameColor() }
                                }
                            }

                            private fun setNameColor() {
                                if (treeTableRow.item != null) {
                                    textFill = if (treeTableRow.item.contentProperty.get() != null) {
                                        Color.BLACK
                                    } else {
                                        Color.GRAY
                                    }
                                }
                            }
                        }
                    }

            column<ConfigFX, Value?>("Value") { param: TreeTableColumn.CellDataFeatures<ConfigFX, Value?> ->
                param.value.value.contentProperty.objectBinding { it as? Value }
            }.setCellFactory {
                ValueChooserCell()
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


//    private var root: ConfigFXTreeItem? = null

//    @FXML
//    private val metaEditorTable: TreeTableView<ConfigFX>? = null
//    @FXML
//    private val nameColumn: TreeTableColumn<ConfigFX, String>? = null
//    @FXML
//    private val valueColumn: TreeTableColumn<ConfigFX, Value>? = null
//    @FXML
//    private val descriptionColumn: TreeTableColumn<ConfigFX, String>? = null
//    @FXML
//    private val valueAddButton: Button? = null
//    @FXML
//    private val nodeAddButton: Button? = null
//    @FXML
//    private val removeButton: Button? = null
//    @FXML
//    private val metaEditorRoot: BorderPane? = null
//

//
//    init {
//        val loader = FXMLLoader(ConfigEditor::class.java.getResource("/fxml/MetaEditor.fxml"))
//        loader.setRoot(this)
//        loader.setController(this)
//
//        try {
//            loader.load<Any>()
//        } catch (ex: Exception) {
//            LoggerFactory.getLogger("FX").error("Error during fxml initialization", ex)
//            throw Error(ex)
//        }
//
//    }
//
//    /**
//     * Initializes the controller class.
//     */
//    override fun initialize(url: URL, rb: ResourceBundle) {
//        //        metaEditorTable.setShowRoot(false);
//        metaEditorTable!!.sortMode = TreeSortMode.ALL_DESCENDANTS
//        nameColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.nameProperty() }
//
//        nameColumn.setCellFactory { param: TreeTableColumn<ConfigFX, String> ->
//            object : TextFieldTreeTableCell<ConfigFX, String>() {
//                override fun updateItem(item: String, empty: Boolean) {
//                    super.updateItem(item, empty)
//                    setNameColor()
//                    if (treeTableRow.item != null) {
//                        treeTableRow.item.valuePresent()
//                                .addListener { observable: Observable -> setNameColor() }
//                    }
//                }
//
//                private fun setNameColor() {
//                    if (treeTableRow.item != null) {
//                        if (treeTableRow.item.valuePresent().get()) {
//                            textFill = Color.BLACK
//                        } else {
//                            textFill = Color.GRAY
//                        }
//                    }
//                }
//            }
//        }
//
//        descriptionColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<ConfigFX, String> -> param.value.value.descriptionProperty() }
//
//        valueColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<ConfigFX, Value> ->
//            val tree = param.value.value
//            if (tree is ConfigFXValue) {
//                return@valueColumn.setCellValueFactory(tree).valueProperty()
//            } else {
//                return@valueColumn.setCellValueFactory null
//            }
//        }
//
//        valueColumn.setCellFactory { column: TreeTableColumn<ConfigFX, Value> -> ValueChooserCell() }
//
//        descriptionColumn.setCellFactory { param: TreeTableColumn<ConfigFX, String> ->
//            val cell = TreeTableCell<ConfigFX, String>()
//            val text = Text()
//            cell.graphic = text
//            cell.prefHeight = Control.USE_COMPUTED_SIZE
//            text.wrappingWidthProperty().bind(param.widthProperty())
//            text.textProperty().bind(cell.itemProperty())
//            cell
//        }
//    }

    //    public final void setRoot(ConfigFXTreeItem rootItem) {
    //        root = rootItem;
    //        root.setExpanded(true);
    //        metaEditorTable.setRoot(root);
    //        if (!rootItem.isLeaf()) {
    //            this.configuration = (rootItem.getValue()).getNode();
    //        } else {
    //            throw new RuntimeException("Can't assign leaf as a root");
    //        }
    //    }

//    fun setRoot(configuration: Configuration, descriptor: NodeDescriptor) {
//        root = ConfigFXTreeItem(ConfigFXNode.build(configuration, descriptor))
//        root!!.isExpanded = true
//        metaEditorTable!!.root = root
//        this.configuration = configuration
//    }


//    private val selectedItem: Optional<ConfigFX>
//        get() = if (metaEditorTable!!.selectionModel.isEmpty) {
//            Optional.empty()
//        } else {
//            Optional.of(this.metaEditorTable.selectionModel.selectedItem.value)
//        }

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

//    @FXML
//    private fun onValueAddClick(event: ActionEvent) {
//        selectedItem.ifPresent { selected ->
//            if (selected is ConfigFXNode) {
//                val valueName = showNameDialog(false)
//                if (valueName != null) {
//                    selected.setValue(valueName, Value.NULL)
//                }
//            }
//        }
//    }
//
//    @FXML
//    private fun onNodeAddClick(event: ActionEvent) {
//        selectedItem.ifPresent { selected ->
//            if (selected is ConfigFXNode) {
//                val nodeName = showNameDialog(false)
//                if (nodeName != null) {
//                    selected.addNode(nodeName)
//                }
//            }
//        }
//    }
//
//    @FXML
//    private fun onRemoveClick(event: ActionEvent) {
//        selectedItem.ifPresent { cfg -> cfg.remove() }
//    }

    private inner class ValueChooserCell : TreeTableCell<ConfigFX, Value?>() {

        private val rowData: ConfigFX?
            get() = treeTableRow.item

        public override fun updateItem(item: Value?, empty: Boolean) {
            if (!empty) {
                val row = rowData
                if (row != null) {
                    if (row is ConfigFXValue) {
                        text = null
                        val chooser = row.valueChooser
                        graphic = chooser.node
                    } else {
                        text = null
                        //                        textProperty().bind(row.stringValueProperty());
                        graphic = null
                        isEditable = false
                    }
                }
            } else {
                text = null
                graphic = null
            }
        }
    }

}

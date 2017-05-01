/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.fx.values.ValueChooser;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Configuration;
import hep.dataforge.values.Value;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class ConfigEditor extends BorderPane implements Initializable, Annotated {

    public static ConfigEditor build(Configuration configuration, NodeDescriptor descriptor) {
        ConfigEditor component = new ConfigEditor();
        component.setRoot(configuration, descriptor);
        return component;
    }

    public ConfigEditor() {
        FXMLLoader loader = new FXMLLoader(ConfigEditor.class.getResource("/fxml/MetaEditor.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (Exception ex) {
            LoggerFactory.getLogger("FX").error("Error during fxml initialization", ex);
            throw new Error(ex);
        }
    }

    /**
     * Link to configuration being edited
     */
    private Configuration configuration;

    private ConfigFXTreeItem root;

    @FXML
    private TreeTableView<ConfigFX> metaEditorTable;
    @FXML
    private TreeTableColumn<ConfigFX, String> nameColumn;
    @FXML
    private TreeTableColumn<ConfigFX, Value> valueColumn;
    @FXML
    private TreeTableColumn<ConfigFX, String> descriptionColumn;
    @FXML
    private Button valueAddButton;
    @FXML
    private Button nodeAddButton;
    @FXML
    private Button removeButton;
    @FXML
    private BorderPane metaEditorRoot;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        metaEditorTable.setShowRoot(false);
        metaEditorTable.setSortMode(TreeSortMode.ALL_DESCENDANTS);
        nameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigFX, String> param)
                        -> param.getValue().getValue().nameProperty()
        );

        nameColumn.setCellFactory((TreeTableColumn<ConfigFX, String> param) ->
                new TextFieldTreeTableCell<ConfigFX, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setNameColor();
                        if (getTreeTableRow().getItem() != null) {
                            getTreeTableRow().getItem().valuePresent()
                                    .addListener((Observable observable) -> {
                                        setNameColor();
                                    });
                        }
                    }

                    private void setNameColor() {
                        if (getTreeTableRow().getItem() != null) {
                            if (getTreeTableRow().getItem().valuePresent().get()) {
                                setTextFill(Color.BLACK);
                            } else {
                                setTextFill(Color.GRAY);
                            }
                        }
                    }
                });

        descriptionColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigFX, String> param)
                        -> param.getValue().getValue().descriptionProperty()
        );

        valueColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigFX, Value> param) -> {
                    ConfigFX tree = param.getValue().getValue();
                    if (tree instanceof ConfigFXValue) {
                        return ((ConfigFXValue) tree).valueProperty();
                    } else {
                        return null;
                    }
                }
        );

        valueColumn.setCellFactory((TreeTableColumn<ConfigFX, Value> column) -> {
            return new ValueChooserCell();
        });

        descriptionColumn.setCellFactory((TreeTableColumn<ConfigFX, String> param) -> {
            TreeTableCell<ConfigFX, String> cell = new TreeTableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(param.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

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

    public final void setRoot(Configuration configuration, NodeDescriptor descriptor) {
        root = new ConfigFXTreeItem(ConfigFXNode.build(configuration, descriptor));
        root.setExpanded(true);
        metaEditorTable.setRoot(root);
        this.configuration = configuration;
    }

    @Override
    public Configuration meta() {
        return configuration;
    }

    private Optional<ConfigFX> getSelectedItem() {
        if (metaEditorTable.getSelectionModel().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(this.metaEditorTable.getSelectionModel().getSelectedItem().getValue());
        }
    }

    private String showNameDialog(boolean forNode) {
        TextInputDialog dialog = new TextInputDialog();
        if (forNode) {
            dialog.setTitle("Node name selection");
            dialog.setContentText("Enter a name for new node: ");
        } else {
            dialog.setTitle("Value name selection");
            dialog.setContentText("Enter a name for new value: ");
        }
        dialog.setHeaderText(null);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @FXML
    private void onValueAddClick(ActionEvent event) {
        getSelectedItem().ifPresent(selected -> {
            if (selected instanceof ConfigFXNode) {
                String valueName = showNameDialog(false);
                if (valueName != null) {
                    ((ConfigFXNode) selected).addValue(valueName, Value.NULL);
                }
            }
        });
    }

    @FXML
    private void onNodeAddClick(ActionEvent event) {
        getSelectedItem().ifPresent(selected -> {
            if (selected instanceof ConfigFXNode) {
                String nodeName = showNameDialog(false);
                if (nodeName != null) {
                    ((ConfigFXNode) selected).addNode(nodeName);
                }
            }
        });
    }

    @FXML
    private void onRemoveClick(ActionEvent event) {
        getSelectedItem().ifPresent(cfg -> cfg.remove());
    }

    private class ValueChooserCell extends TreeTableCell<ConfigFX, Value> {

        @Override
        public void updateItem(Value item, boolean empty) {
            if (!empty) {
                ConfigFX row = getRowData();
                if (row != null) {
                    if (row instanceof ConfigFXValue) {
                        setText(null);
                        ValueChooser chooser = ((ConfigFXValue) row).valueChooser();
                        setGraphic(chooser.getNode());
                    } else {
                        setText(null);
//                        textProperty().bind(row.stringValueProperty());
                        setGraphic(null);
                        setEditable(false);
                    }
                }
            } else {
                setText(null);
                setGraphic(null);
            }
        }

        private ConfigFX getRowData() {
            return getTreeTableRow().getItem();
        }
    }
}

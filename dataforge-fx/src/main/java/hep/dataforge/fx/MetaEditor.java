/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.fx.values.ValueChooser;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class MetaEditor extends AnchorPane implements Initializable, Annotated {
    
    public static MetaEditor build(Configuration configuration, NodeDescriptor descriptor) {
        MetaEditor component = new MetaEditor();
        component.setRoot(configuration, descriptor);
        return component;
    }
    
    public MetaEditor() {
        FXMLLoader loader = new FXMLLoader(MetaEditor.class.getResource("/fxml/MetaEditor.fxml"));
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
    
    private MetaTreeItem root;
    
    @FXML
    private TreeTableView<MetaTree> metaEditorTable;
    @FXML
    private TreeTableColumn<MetaTree, String> nameColumn;
    @FXML
    private TreeTableColumn<MetaTree, Value> valueColumn;
    @FXML
    private TreeTableColumn<MetaTree, String> descriptionColumn;
    @FXML
    private Button valueAddButton;
    @FXML
    private Button nodeAddButton;
    @FXML
    private Button removeButton;
    @FXML
    private AnchorPane metaEditorRoot;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        metaEditorTable.setShowRoot(false);
        metaEditorTable.setSortMode(TreeSortMode.ALL_DESCENDANTS);
        nameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<MetaTree, String> param)
                -> param.getValue().getValue().nameValue()
        );
        
        nameColumn.setCellFactory((TreeTableColumn<MetaTree, String> param) -> {
            return new TextFieldTreeTableCell<MetaTree, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setNameColor();
                    if (getTreeTableRow().getItem() != null) {
                        getTreeTableRow().getItem().isDefault()
                                .addListener((Observable observable) -> {
                            setNameColor();
                        });
                    }
                }
                
                private void setNameColor() {
                    if (getTreeTableRow().getItem() != null) {
                        if (getTreeTableRow().getItem().isDefault().get()) {
                            setTextFill(Color.GRAY);
                        } else {
                            setTextFill(Color.BLACK);
                        }
                    }
                }
            };
        });
        
        descriptionColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<MetaTree, String> param)
                -> param.getValue().getValue().descriptionValue()
        );
        
        valueColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<MetaTree, Value> param) -> {
                    MetaTree tree = param.getValue().getValue();
                    if (tree.isNode()) {
                        return null;
                    } else {
                        return ((MetaTreeLeaf) tree).value();
                    }
                }
        );
        
        valueColumn.setCellFactory((TreeTableColumn<MetaTree, Value> column) -> {
            return new ValueChooserCell();
        });
        
        descriptionColumn.setCellFactory((TreeTableColumn<MetaTree, String> param) -> {
            TreeTableCell<MetaTree, String> cell = new TreeTableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(param.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        
    }
    
    public final void setRoot(MetaTreeItem rootItem) {
        root = rootItem;
        root.setExpanded(true);
        metaEditorTable.setRoot(root);
        if (!rootItem.isLeaf()) {
            this.configuration = ((MetaTreeBranch) rootItem.getValue()).getNode();
        } else {
            throw new RuntimeException("Can't assign leaf as a root");
        }
    }
    
    public final void setRoot(Configuration configuration, NodeDescriptor descriptor) {
        root = new MetaTreeItem(configuration, descriptor);
        root.setExpanded(true);
        metaEditorTable.setRoot(root);
        this.configuration = configuration;
    }
    
    public void addNode(MetaTreeItem parent, Meta node, NodeDescriptor descriptor) {
        parent.addBranch(node, descriptor);
    }
    
    public void addValue(MetaTreeItem parent, String valueName) {
        parent.addLeaf(valueName);
    }
    
    @Override
    public Configuration meta() {
        return configuration;
    }
    
    private MetaTreeItem getSelectedItem() {
        if (metaEditorTable.getSelectionModel().isEmpty()) {
            return null;
        } else {
            return (MetaTreeItem) this.metaEditorTable.getSelectionModel().getSelectedItem();
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
        MetaTreeItem selected = getSelectedItem();
        if (selected != null) {
            String valueName = showNameDialog(false);
            if (valueName != null) {
                selected.addLeaf(valueName);
            }
        }
    }
    
    @FXML
    private void onNodeAddClick(ActionEvent event) {
        MetaTreeItem selected = getSelectedItem();
        if (selected != null) {
            String nodeName = showNameDialog(true);
            if (nodeName != null) {
                selected.addBranch(nodeName);
            }
        }
    }
    
    @FXML
    private void onRemoveClick(ActionEvent event) {
        MetaTreeItem selected = getSelectedItem();
        if (selected != null) {
            selected.remove();
        }
    }
    
    public TreeTableView<MetaTree> geTable() {
        return metaEditorTable;
    }
    
    private class ValueChooserCell extends TreeTableCell<MetaTree, Value> {
        
        @Override
        public void updateItem(Value item, boolean empty) {
            if (!empty) {
                MetaTree row = getRowData();
                if (row != null) {
                    if (row.isNode()) {
                        setText(null);
//                        textProperty().bind(row.stringValueProperty());
                        setGraphic(null);
                        setEditable(false);
                    } else {
                        setText(null);
                        MetaTreeLeaf leaf = (MetaTreeLeaf) row;
                        ValueChooser chooser = leaf.valueChooser();
                        setGraphic(chooser.getNode());
                    }
                }
            } else {
                setText(null);
                setGraphic(null);
            }
        }
        
        private MetaTree getRowData() {
            return getTreeTableRow().getItem();
        }
    }
}

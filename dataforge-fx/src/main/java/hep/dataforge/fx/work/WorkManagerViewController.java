/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.work;

import hep.dataforge.computation.Task;
import hep.dataforge.computation.TaskManager;
import hep.dataforge.utils.Utils;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class WorkManagerViewController implements Initializable {

    private final Map<Task, Parent> processNodeCache = Utils.<Task, Parent>getLRUCache(400);
    @FXML
    private TreeView<Task> processTreeView;

    public static BorderPane build(TaskManager manager) {
        try {
            FXMLLoader loader = new FXMLLoader(manager.getClass().getResource("/fxml/ProcessManagerView.fxml"));
            BorderPane p = loader.load();
            WorkManagerViewController controller = loader.getController();
            controller.setRoot(manager.getRoot());
            return p;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        processTreeView.setCellFactory((TreeView<Task> param) -> new TreeCell<Task>() {
            @Override
            public void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("");
                    //caching processes to insure no drawing lags
                    setGraphic(processNodeCache.computeIfAbsent(item, p -> WorkViewController.build(p)));
                }
            }
        });
    }

    public void setRoot(Task rootTask) {
        TreeItem<Task> root = buildTree(rootTask);
        Platform.runLater(() -> processTreeView.setRoot(root));
    }

    //FXME concurrent modification
    private TreeItem<Task> buildTree(Task proc) {
        TreeItem<Task> res = new TreeItem<>(proc);
        res.setExpanded(true);
        proc.getChildren().values().stream().forEach((Task child) -> {
            res.getChildren().add(buildTree(child));
        });

        proc.getChildren().addListener((MapChangeListener.Change<? extends String, ? extends Task> change) -> {
            if (change.wasAdded()) {
                res.getChildren().add(buildTree(change.getValueAdded()));
            }
            if (change.wasRemoved()) {
                res.getChildren().removeIf(item -> item.getValue().equals(change.getValueRemoved()));
            }
        });

        return res;
    }

}

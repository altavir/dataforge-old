/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.process;

import hep.dataforge.work.Work;
import hep.dataforge.work.WorkManager;
import hep.dataforge.fx.FXUtils;
import hep.dataforge.utils.CommonUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
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

/**
 * FXML Controller class
 *
 * @author Alexander Nozik
 */
public class ProcessManagerViewController implements Initializable {

    public static BorderPane build(WorkManager manager) {
        try {
            FXMLLoader loader = new FXMLLoader(manager.getClass().getResource("/fxml/ProcessManagerView.fxml"));
            BorderPane p = loader.load();
            ProcessManagerViewController controller = loader.getController();
            controller.setManager(manager);
            return p;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private WorkManager manager;
    private final Map<Work, Parent> processNodeCache = CommonUtils.<Work, Parent>getLRUCache(400);

    @FXML
    private TreeView<Work> processTreeView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        processTreeView.setCellFactory((TreeView<Work> param) -> new TreeCell<Work>() {
            @Override
            public void updateItem(Work item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("");
                    //caching processes to insure no drawing lags
                    setGraphic(processNodeCache.computeIfAbsent(item, p -> ProcessViewController.build(p)));
                }
            }
        });
    }

    public void setManager(WorkManager manager) {
        this.manager = manager;
        FXUtils.runNow(() -> processTreeView.setRoot(buildTree(manager.getRoot())));
    }

    private synchronized TreeItem<Work> buildTree(Work proc) {
        TreeItem<Work> res = new TreeItem<>(proc);
        res.setExpanded(true);
//        res.setGraphic(ProcessViewController.build(proc));
        proc.getChildren().values().stream().forEach(new Consumer<Work>() {
            @Override
            public synchronized void accept(Work child) {
                res.getChildren().add(buildTree(child));
            }
        });

        proc.getChildren().addListener(new MapChangeListener<String, Work>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends Work> change) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        res.getChildren().add(buildTree(change.getValueAdded()));
                    }
                    if (change.wasRemoved()) {
                        res.getChildren().removeIf(item -> item.getValue().equals(change.getValueRemoved()));
                    }

                });
            }
        });

        return res;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.work;

import hep.dataforge.computation.TaskManager;
import hep.dataforge.context.Context;
import hep.dataforge.fx.FXFragment;
import hep.dataforge.fx.FXUtils;
import hep.dataforge.utils.NonNull;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class WorkManagerFragment extends FXFragment {

    private final AnchorPane root = new AnchorPane();
    private TaskManager manager;

    public WorkManagerFragment() {
    }

    public WorkManagerFragment(TaskManager manager) {
        this.manager = manager;
    }

    /**
     * Build new FXProcessManager, attach it to context and create window for it
     *
     * @param context
     * @return
     */
    public static WorkManagerFragment attachToContext(Context context) {
        TaskManager manager = context.taskManager();
        return new WorkManagerFragment(manager);
    }

    @Override
    protected Stage buildStage(Parent root) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, 400, 400);
        stage.sizeToScene();
        stage.setTitle("DataForge task manager");
        stage.setScene(scene);

        return stage;
    }

    public TaskManager getManager() {
        return manager;
    }

    public void setManager(@NonNull TaskManager manager) {
        this.manager = manager;
        FXUtils.runNow(() -> {
            root.getChildren().clear();
            BorderPane node = WorkManagerViewController.build(manager);
            AnchorPane.setBottomAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            root.getChildren().add(node);
        });
    }

    @Override
    protected AnchorPane getRoot() {
        return root;
    }

}

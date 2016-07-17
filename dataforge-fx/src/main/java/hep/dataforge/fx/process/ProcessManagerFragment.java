/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.process;

import hep.dataforge.context.Context;
import hep.dataforge.work.WorkManager;
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
public class ProcessManagerFragment extends FXFragment {

    private WorkManager manager;
    private final AnchorPane root = new AnchorPane();

    /**
     * Build new FXProcessManager, attach it to context and create window for it
     *
     * @param context
     * @return
     */
    public static ProcessManagerFragment attachToContext(Context context) {
        WorkManager manager = context.workManager();
        return new ProcessManagerFragment(manager);
    }

    public ProcessManagerFragment() {
    }

    public ProcessManagerFragment(WorkManager manager) {
        this.manager = manager;
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

    public void setManager(@NonNull WorkManager manager) {
        this.manager = manager;
        FXUtils.runNow(() -> {
            root.getChildren().clear();
            BorderPane node = ProcessManagerViewController.build(manager);
            AnchorPane.setBottomAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            root.getChildren().add(node);
        });
    }

    public WorkManager getManager() {
        return manager;
    }

    @Override
    protected AnchorPane getRoot() {
        return root;
    }

}

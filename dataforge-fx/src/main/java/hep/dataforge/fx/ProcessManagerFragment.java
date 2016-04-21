/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.context.Context;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Alexander Nozik
 */
public class ProcessManagerFragment extends FXFragment {

    private final FXProcessManager manager;

    /**
     * Build new FXProcessManager, attach it to context and create window for it
     *
     * @param context
     * @return
     */
    public static ProcessManagerFragment attachToContext(Context context) {
        FXProcessManager manager = new FXProcessManager();
        context.setProcessManager(manager);
        return new ProcessManagerFragment(manager);
    }

    public ProcessManagerFragment(FXProcessManager manager) {
        this.manager = manager;
    }

    @Override
    protected Stage buildStage() {
        Stage stage = new Stage();
        AnchorPane pane = new AnchorPane();
        manager.show(pane);
        Scene scene = new Scene(pane, 400, 400);
        stage.sizeToScene();
        stage.setTitle("DataForge task manager");
        stage.setScene(scene);

        return stage;
    }

    public FXProcessManager getManager() {
        return manager;
    }

}

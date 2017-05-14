/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.work;

import hep.dataforge.fx.fragments.FXFragment;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Nozik
 */
public class WorkManagerFragment extends FXFragment {

    private WorkManager manager;

    public WorkManagerFragment() {
        super("DataForge task manager", 400, 400);
    }

    public WorkManagerFragment(WorkManager manager) {
        this();
        setManager(manager);
    }

//    /**
//     * Build new FXProcessManager, attach it to context and create window for it
//     *
//     * @param context
//     * @return
//     */
//    public static WorkManagerFragment start(Context context) {
//        WorkManager manager = context.get(WorkManager.class);
//        return new WorkManagerFragment(manager);
//    }


    public WorkManager getManager() {
        return manager;
    }

    public final void setManager(@NotNull WorkManager manager) {
        this.manager = manager;
    }

    @Override
    protected AnchorPane buildRoot() {
        AnchorPane root = new AnchorPane();
        root.getChildren().clear();
        BorderPane node = WorkManagerViewController.build(manager);
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);
        root.getChildren().add(node);
        return root;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.work;

import hep.dataforge.context.Context;
import hep.dataforge.fx.fragments.Fragment;
import hep.dataforge.goals.TaskManager;
import hep.dataforge.utils.NonNull;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

/**
 * @author Alexander Nozik
 */
public class WorkManagerFragment extends Fragment {

    private TaskManager manager;

    public WorkManagerFragment() {
        super("DataForge task manager", 400, 400);
    }

    public WorkManagerFragment(TaskManager manager) {
        this();
        setManager(manager);
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


    public TaskManager getManager() {
        return manager;
    }

    public final void setManager(@NonNull TaskManager manager) {
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

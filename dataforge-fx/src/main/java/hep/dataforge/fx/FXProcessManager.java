/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.context.Process;
import hep.dataforge.context.ProcessManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.TaskProgressView;

/**
 *
 * @author Alexander Nozik
 */
public class FXProcessManager extends ProcessManager {

    private final TaskProgressView view = new TaskProgressView();
    private final Map<String, Task> tasks = new HashMap<>();
    
    @Override
    public synchronized <U> Process<U> post(String processName, Supplier<U> sup) {
        Task<U> task = new Task<U>() {
            @Override
            protected U call() throws Exception {
                updateTitle(processName);
                return sup.get();
            }
        };
        tasks.put(processName, task);
        Platform.runLater(() -> view.getTasks().add(task));
        return super.post(processName, () -> task.getValue());
    }

    @Override
    public synchronized Process post(String processName, Runnable runnable) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateTitle(processName);
                runnable.run();
                return null;
            }
        };
        tasks.put(processName, task);
        Platform.runLater(() -> view.getTasks().add(task));
        return super.post(processName, task);
    }

    @Override
    protected void onProcessResult(String processName, Object result) {
        super.onProcessResult(processName, result);
    }

    @Override
    protected void onProcessException(String processName, Throwable exception) {
        super.onProcessException(processName, exception);
    }

    @Override
    protected void onProcessFinished(String processName) {
        super.onProcessFinished(processName);
    }

    @Override
    protected void onProcessStarted(String processName) {
        super.onProcessStarted(processName);
    }

    public TaskProgressView getView() {
        return view;
    }

    public void show(AnchorPane pane) {
        pane.getChildren().clear();
        pane.getChildren().add(view);
        AnchorPane.setBottomAnchor(view, 0d);
        AnchorPane.setTopAnchor(view, 0d);
        AnchorPane.setLeftAnchor(view, 0d);
        AnchorPane.setRightAnchor(view, 0d);
    }

}

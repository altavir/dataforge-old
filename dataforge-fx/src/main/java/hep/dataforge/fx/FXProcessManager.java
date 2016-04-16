/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.context.Process;
import hep.dataforge.context.ProcessManager;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.TaskProgressView;

/**
 * A process manager with JavaFx graphical representation
 *
 * @author Alexander Nozik
 */
public class FXProcessManager extends ProcessManager {

    //TODO remake from scratch
    private final TaskProgressView view = new TaskProgressView();
//    private final Map<String, ProcessTask> tasks = new HashMap<>();

    @Override
    public synchronized <U> Process post(String processName, CompletableFuture<U> task) {
        Process proc = super.post(processName, task);
        ProcessTask fxTask = new ProcessTask(proc);
        Platform.runLater(() -> {
            view.getTasks().add(fxTask);
        });
        return proc;
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

    private class ProcessTask extends Task {

        private final Process proc;

        public ProcessTask(Process proc) {
            this.proc = proc;
            updateMessage(proc.getMessage());
            proc.messageProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                updateMessage(newValue);
            });
            updateTitle(proc.getTitle());
            proc.titleProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                updateTitle(newValue);
            });
            updateProgress(proc.getProgress(), proc.getMaxProgress());
            proc.progressProperty().addListener((Observable observable) -> {
                updateProgress(proc.getProgress(), proc.getMaxProgress());
            });
            proc.maxProgressProperty().addListener((Observable observable) -> {
                updateProgress(proc.getProgress(), proc.getMaxProgress());
            });
        }

        @Override
        protected Object call() throws Exception {
            return proc.getTask().get();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return proc.getTask().cancel(mayInterruptIfRunning);
        }

    }

}

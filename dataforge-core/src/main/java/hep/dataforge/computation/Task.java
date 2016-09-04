/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * <p>
 * WARNING! While Work uses JavaFX beans API, it is not run on JavaFX UI thread.
 * In order to bind variables to UI components, one needs to wrap all UI calls
 * into Platform.runLater.
 * </p>
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public class Task implements Named {

    private final String name;

    private final ObjectProperty<CompletableFuture<?>> taskProperty = new SimpleObjectProperty<>();

    private final ObservableMap<String, Task> children = FXCollections.<String, Task>observableHashMap();

    private final DoubleProperty curMaxProgress;

    private final DoubleProperty curProgress;

    /**
     * Titile for current task (by default equals name)
     */
    private final StringProperty title;

    /**
     * Message for current task
     */
    private final StringProperty message;

    /**
     * Total process progress including children
     */
    private final DoubleBinding totalProgress;

    private final DoubleBinding totalMaxProgress;

    private final BooleanBinding isDone;

    /**
     * The manager to which this process is attached
     */
    private final TaskManager manager;

    public Task(TaskManager manager, String name) {
        this.name = name;
        this.manager = manager;
        this.curProgress = new SimpleDoubleProperty(0);

        totalProgress = new DoubleBinding() {
            @Override
            protected double computeValue() {
                return getProgress();
            }
        };

        totalMaxProgress = new DoubleBinding() {
            @Override
            protected double computeValue() {
                return getMaxProgress();
            }
        };

        curProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            totalProgress.invalidate();
        });

        this.curMaxProgress = new SimpleDoubleProperty(0);

        curMaxProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            totalMaxProgress.invalidate();
        });

        isDone = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return (taskProperty.get() == null || taskProperty.get().isDone())
                        && children.values().stream().allMatch((Task p) -> p.isDone());
            }
        };

        this.title = new SimpleStringProperty(name);
        this.message = new SimpleStringProperty("");
    }

    /**
     * Unmodifiable map of children
     *
     * @return
     */
    public ObservableMap<String, Task> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return name;
    }

    public ObjectProperty<CompletableFuture<?>> taskProperty() {
        return taskProperty;
    }

    public CompletableFuture<?> getTask() {
        return taskProperty.get();
    }

    protected void setTask(CompletableFuture<?> task) {
        if (this.taskProperty.get() != null) {
            throw new RuntimeException("The task for this process already set");
        }
        getManager().onStarted(getName());
//        if (task.isDone()) {
//            curProgress.set(curMaxProgress.get());
//        }

        task.whenCompleteAsync((res, ex) -> {
            curProgress.set(curMaxProgress.get());
            isDone.invalidate();
            getManager().onFinished(getName());
        });

        taskProperty.set(task);
        isDone.invalidate();
    }

    public Task findProcess(String processName) {
        return findProcess(Name.of(processName));
    }

    /**
     * Find the child process with the given name. Empty name returns this
     * process
     *
     * @param processName
     * @return null if process not found
     */
    public Task findProcess(Name processName) {
        if (processName.entry().isEmpty()) {
            return this;
        }
        if (this.children.containsKey(processName.entry())) {
            if (processName.length() == 1) {
                return this.children.get(processName.entry());
            } else {
                return this.children.get(processName.entry()).findProcess(processName.cutFirst());
            }
        } else {
            return null;
        }
    }

    Task addChild(String childName, CompletableFuture<?> future) {
        return addChild(Name.of(childName), future);
    }

    Task addChild(Name childName, CompletableFuture<?> future) {
        if (childName.length() == 1) {
            return addDirectChild(childName.toString(), future);
        } else {
            Task childProcess;
            if (children.containsKey(childName.getFirst().toString())) {
                childProcess = children.get(childName.getFirst().toString());
            } else {
                //create empty child process to maintain tree structure
                childProcess = addDirectChild(childName.getFirst().toString(), null);
            }
            return childProcess.addChild(childName.cutFirst(), future);
        }
    }

    /**
     * Add a child with the simple name (no path)
     *
     * @param childName a name of child process without path notation
     * @param future could be null
     */
    protected Task addDirectChild(String childName, CompletableFuture<?> future) {
        if (this.children.containsKey(childName) && !this.children.get(childName).isDone()) {
            throw new RuntimeException("Triyng to replace existing running process with the same name.");
        }

        Task childProcess = new Task(getManager(), Name.join(getName(), childName).toString());
        if (future != null) {
            childProcess.setTask(future);
        }
        this.children.put(childName, childProcess);
        //Add listeners for progress

        childProcess.totalProgress.addListener((Observable observable) -> {
            totalProgress.invalidate();
        });

        childProcess.totalMaxProgress.addListener((Observable observable) -> {
            totalMaxProgress.invalidate();
        });

        //invalidating isDone in case of child state change
        childProcess.isDone.addListener((Observable observable) -> {
            isDone.invalidate();
        });

        // Revalidate completions
        isDone.invalidate();

        return childProcess;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return titleProperty().get();
    }

    public void setTitle(String title) {
        this.titleProperty().set(title);
    }

    public StringProperty messageProperty() {
        return message;
    }

    public String getMessage() {
        return messageProperty().get();
    }

    public void setMessage(String message) {
        this.messageProperty().set(message);
    }

    public ObservableDoubleValue progressProperty() {
        return totalProgress;
    }

    public ObservableDoubleValue maxProgressProperty() {
        return totalMaxProgress;
    }

    public double getProgress() {
        return curProgress.get() + children.values().stream().mapToDouble(it -> it.getProgress()).sum();
    }

    /**
     * Set progress values to given values
     *
     * @param progress
     */
    public void setProgress(double progress) {
        this.curProgress.set(progress);
    }

    public double getMaxProgress() {
        return curMaxProgress.get() + children.values().stream().mapToDouble(it -> it.getMaxProgress()).sum();
    }

    public void setMaxProgress(double maxProgress) {
        this.curMaxProgress.set(maxProgress);
    }

    public BooleanBinding isDoneProperty() {
        return isDone;
    }

    /**
     * Increase progress by given value
     *
     * @param incProgress
     */
    public void increaseProgress(double incProgress) {
        this.curProgress.set(this.curProgress.get() + incProgress);
//        this.curMaxProgress.set(this.curMaxProgress.get() + incProgress);
    }

    /**
     * Set current progress to max progress.
     */
    public void setProgressToMax() {
        this.curProgress.set(this.curMaxProgress.get());
    }

    /**
     * The process is considered done if its own task and all of child tasks are
     * complete (either with result or exceptionally)
     *
     * @return
     */
    public boolean isDone() {
        isDoneProperty().invalidate();
        return isDoneProperty().get();
    }

    /**
     * Cancel this process and all child processes
     *
     * @param interrupt
     */
    public void cancel(boolean interrupt) {
        if (this.taskProperty.get() != null) {
            this.taskProperty.get().cancel(interrupt);
        }
        this.children.values().forEach((Task p) -> p.cancel(interrupt));
    }

    /**
     * Remove completed processes
     */
    public synchronized void cleanup() {
        children.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isDone())
                .map(entry -> entry.getKey())
                .collect(Collectors.toList()).stream().forEach((childName) -> this.children.remove(childName));
        this.children.forEach((String procName, Task proc) -> proc.cleanup());
    }

    /**
     * @return the manager
     */
    public TaskManager getManager() {
        return manager;
    }

    public ProgressCallback callback() {
        return getManager().callback(this.getName());
    }
}

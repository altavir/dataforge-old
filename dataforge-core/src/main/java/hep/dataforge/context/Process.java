/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.names.AnonimousNotAlowed;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public class Process implements Named {

    private final String name;

    private final ObjectProperty<CompletableFuture> taskProperty = new SimpleObjectProperty<>();

    private final ObservableMap<String, Process> children = FXCollections.observableHashMap();

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

    public Process(String name) {
        this.name = name;
        this.curProgress = new SimpleDoubleProperty(-1);

        totalProgress = new DoubleBinding() {
            @Override
            protected double computeValue() {
                return getProgress();
            }
        };

        totalMaxProgress = new DoubleBinding() {
            @Override
            protected double computeValue() {
                return getProgress();
            }
        };

        curProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            totalProgress.invalidate();
        });

        this.curMaxProgress = new SimpleDoubleProperty(1);

        curMaxProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            totalMaxProgress.invalidate();
        });

        isDone = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return (taskProperty.get() == null || taskProperty.get().isDone())
                        && children.values().stream().allMatch((Process p) -> p.isDone());
            }
        };

        this.title = new SimpleStringProperty(name);
        this.message = new SimpleStringProperty("");
    }

    @Override
    public String getName() {
        return name;
    }

    public ObjectProperty<CompletableFuture> taskProperty() {
        return taskProperty;
    }

    void setTask(CompletableFuture<?> task) {
        if (this.taskProperty.get() != null) {
            throw new RuntimeException("The task for this process already set");
        }
        taskProperty.set(task.whenComplete((Object t, Throwable u) -> {
            isDone.invalidate();
            curProgress.set(curMaxProgress.get());
        }).whenComplete(this::handle));
        isDone.invalidate();
    }

    public CompletableFuture<?> getTask() {
        return taskProperty.get();
    }

    /**
     * Handle task result. By default does nothing. Reserved for extensions
     *
     * @param result
     * @param exception
     */
    protected void handle(Object result, Throwable exception) {

    }

    public Process findProcess(String processName) {
        return findProcess(Name.of(processName));
    }

    /**
     * Find the child process with the given name. Empty name returns this
     * process
     *
     * @param processName
     * @return null if process not found
     */
    public Process findProcess(Name processName) {
        if (processName.entry().isEmpty()) {
            return this;
        }
        if (this.children.containsKey(processName.entry())) {
            return this.children.get(processName.entry()).findProcess(processName.cutFirst());
        } else {
            return null;
        }
    }

    public Process addChild(String childName, CompletableFuture<?> future) {
        return addChild(Name.of(childName), future);
    }

    public Process addChild(Name childName, CompletableFuture<?> future) {
        if (childName.length() == 1) {
            return addDirectChild(childName.toString(), future);
        } else {
            Process childProcess;
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
    protected Process addDirectChild(String childName, CompletableFuture<?> future) {
        if (this.children.containsKey(childName) && !this.children.get(childName).isDone()) {
            throw new RuntimeException("Triyng to replace existing running process with the same name.");
        }

        Process childProcess = new Process(Name.join(getName(), childName).toString());
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

    public StringProperty messageProperty() {
        return message;
    }

    public String getMessage() {
        return messageProperty().get();
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

    public double getMaxProgress() {
        return curMaxProgress.get() + children.values().stream().mapToDouble(it -> it.getMaxProgress()).sum();
    }

    public BooleanBinding isDoneProperty() {
        return isDone;
    }

    public void setMessage(String message) {
        this.messageProperty().set(message);
    }

    public void setTitle(String title) {
        this.titleProperty().set(title);
    }

    public void setProgress(double progress, double maxProgress) {
        this.curProgress.set(progress);
        this.curMaxProgress.set(maxProgress);
    }

    /**
     * The process is considered done if its own task and all of child tasks are
     * complete (either with result or exceptionally)
     *
     * @return
     */
    public boolean isDone() {
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
        this.children.values().forEach((Process p) -> p.cancel(interrupt));
    }

    /**
     * Remove completed processes
     */
    public synchronized void cleanup() {
        for (String childName : children.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isDone())
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())) {
            this.children.remove(childName);
        }
        this.children.forEach((String procName, Process proc) -> proc.cleanup());
    }

}

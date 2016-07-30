/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

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
 * <p>
 * WARNING! While Work uses JavaFX beans API, it is not run on JavaFX UI thread.
 * In order to bind variables to UI components, one needs to wrap all UI calls
 * into Platform.runLater.
 * </p>
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public class Work<R> implements Named {

    private final String name;

    private final ObjectProperty<CompletableFuture<R>> taskProperty = new SimpleObjectProperty<>();

    private final ObservableMap<String, Work> children = FXCollections.<String, Work>observableHashMap();

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
    private final WorkManager manager;

    public Work(WorkManager manager, String name) {
        this.name = name;
        this.manager = manager;
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
                        && children.values().stream().allMatch((Work p) -> p.isDone());
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
    public synchronized ObservableMap<String, Work> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return name;
    }

    public ObjectProperty<CompletableFuture<R>> taskProperty() {
        return taskProperty;
    }

    protected void setTask(CompletableFuture<R> task) {
        if (this.taskProperty.get() != null) {
            throw new RuntimeException("The task for this process already set");
        }
        task.whenComplete((Object t, Throwable u) -> {
            isDone.invalidate();
            curProgress.set(curMaxProgress.get());
            handle(t, u);
        });

        if (task.isDone()) {
            curProgress.set(curMaxProgress.get());
        }

        taskProperty.set(task);
        isDone.invalidate();
    }

    public CompletableFuture<R> getTask() {
        return taskProperty.get();
    }

    /**
     * Handle task result. By default does nothing. Reserved for extensions
     *
     * @param result
     * @param exception
     */
    protected void handle(Object result, Throwable exception) {
        if (exception != null) {
            getManager().getContext().getLogger().error("Exception in process execution", exception);
        }
    }

    public Work findProcess(String processName) {
        return findProcess(Name.of(processName));
    }

    /**
     * Find the child process with the given name. Empty name returns this
     * process
     *
     * @param processName
     * @return null if process not found
     */
    public Work findProcess(Name processName) {
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

    <T> Work<T> addChild(String childName, CompletableFuture<T> future) {
        return addChild(Name.of(childName), future);
    }

    <T> Work<T> addChild(Name childName, CompletableFuture<T> future) {
        if (childName.length() == 1) {
            return addDirectChild(childName.toString(), future);
        } else {
            Work childProcess;
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
    protected <T> Work<T> addDirectChild(String childName, CompletableFuture<T> future) {
        if (this.children.containsKey(childName) && !this.children.get(childName).isDone()) {
            throw new RuntimeException("Triyng to replace existing running process with the same name.");
        }

        Work childProcess = new Work(getManager(), Name.join(getName(), childName).toString());
        getManager().onStarted(childProcess.getName());
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

    /**
     * Set progress values to given values
     *
     * @param progress
     * @param maxProgress
     */
    public void setProgress(double progress) {
        this.curProgress.set(progress);
    }

    public void setMaxProgress(double maxProgress) {
        this.curMaxProgress.set(maxProgress);
    }

    /**
     * Increase progress by given value
     *
     * @param incProgress
     * @param incMaxProgress
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
        this.children.values().forEach((Work p) -> p.cancel(interrupt));
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
        this.children.forEach((String procName, Work proc) -> proc.cleanup());
    }

    /**
     * @return the manager
     */
    public WorkManager getManager() {
        return manager;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * An visualisation for task tree. This class used only for visualisation and user interrupts.
 * <p>
 * WARNING! While {@code Task} uses JavaFX beans API, it is not run on JavaFX UI thread.
 * In order to bind variables to UI components, one needs to wrap all UI calls
 * into Platform.runLater.
 * </p>
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public class Work implements Named {

    private final String name;

    private final ObjectProperty<CompletableFuture<?>> futureProperty = new SimpleObjectProperty<>();

    private final ObservableMap<String, Work> children = FXCollections.observableHashMap();

    private final DoubleProperty curMaxProgress;

    private final DoubleProperty curProgress;

    /**
     * Title for current task (by default equals name)
     */
    private final StringProperty title;

    /**
     * Message for current task
     */
    private final StringProperty status;

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

        curProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> totalProgress.invalidate());

        this.curMaxProgress = new SimpleDoubleProperty(0);

        curMaxProgress.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> totalMaxProgress.invalidate());

        isDone = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return (futureProperty.get() == null || futureProperty.get().isDone())
                        && children.values().stream().allMatch(Work::isDone);
            }
        };

        this.title = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty("");
    }

    /**
     * Unmodifiable map of children
     *
     * @return
     */
    public ObservableMap<String, Work> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return name;
    }

    public ObjectProperty<CompletableFuture<?>> taskProperty() {
        return futureProperty;
    }

    public CompletableFuture<?> getFuture() {
        return futureProperty.get();
    }

    protected void setFuture(CompletableFuture<?> task) {
        if (this.futureProperty.get() != null && !getFuture().isDone()) {
            throw new RuntimeException("The task for this process already set");
        }
        getManager().onStarted(getName());

        task.whenCompleteAsync((res, ex) -> {
            curProgress.set(curMaxProgress.get());
            isDone.invalidate();
            getManager().onFinished(getName());
        });

        futureProperty.set(task);
        isDone.invalidate();
    }

    public Work find(String subTaskName) {
        return find(Name.of(subTaskName));
    }

    /**
     * Find the child process with the given name. Empty name returns this
     * process
     *
     * @param subTaskName
     * @return null if process not found
     */
    public Work find(Name subTaskName) {
        if (subTaskName.entry().isEmpty()) {
            return this;
        }
        if (this.children.containsKey(subTaskName.entry())) {
            if (subTaskName.length() == 1) {
                return this.children.get(subTaskName.entry());
            } else {
                return this.children.get(subTaskName.entry()).find(subTaskName.cutFirst());
            }
        } else {
            return null;
        }
    }

    public Work addChild(String childName) {
        return addChild(childName, null);
    }

    public Work addChild(String childName, CompletableFuture<?> future) {
        return addChild(Name.of(childName), future);
    }

    public Work addChild(Name childName, CompletableFuture<?> future) {
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
     * @param future    could be null
     */
    private Work addDirectChild(String childName, @Nullable CompletableFuture<?> future) {
        if (this.children.containsKey(childName) && !this.children.get(childName).isDone()) {
            throw new RuntimeException("Trying to replace existing running process with the same name.");
        }

        Work childProcess = new Work(getManager(), Name.join(getName(), childName).toString());
        if (future != null) {
            childProcess.setFuture(future);
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

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return statusProperty().get();
    }

    public void setStatus(String status) {
        this.statusProperty().set(status);
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
        if (this.futureProperty.get() != null) {
            this.futureProperty.get().cancel(interrupt);
        }
        this.children.values().forEach((Work p) -> p.cancel(interrupt));
    }

    /**
     * Remove completed processes
     */
    public synchronized void cleanup() {
        children.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()).forEach(this.children::remove);
        this.children.forEach((String procName, Work work) -> work.cleanup());
    }

    /**
     * @return the manager
     */
    public WorkManager getManager() {
        return manager;
    }
}

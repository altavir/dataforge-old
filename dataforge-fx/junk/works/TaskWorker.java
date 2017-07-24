package hep.dataforge.fx.works;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An extended worker containing primary (already existing) worker and dependencies
 *
 * @param <V>
 */
public class TaskWorker<V> implements ExtendedWorker<V> {
    private final Worker<V> task;
    private final ObservableList<Worker<?>> children = FXCollections.observableArrayList();

    private final DoubleProperty totalProgressProperty = new SimpleDoubleProperty();
    private final DoubleProperty progressProperty = new SimpleDoubleProperty();
    private final DoubleProperty relativeProgressProperty = new SimpleDoubleProperty();

    public TaskWorker(Worker<V> task) {
        this.task = task;

        DoubleBinding totalProgress = new DoubleBinding() {
            {
                bind(task.totalWorkProperty(), children);
            }

            @Override
            protected double computeValue() {
                return task.getTotalWork() + children.stream().mapToDouble(Worker::getTotalWork).sum();
            }
        };

        DoubleBinding progress = new DoubleBinding() {
            {
                bind(task.workDoneProperty(), children);
            }

            @Override
            protected double computeValue() {
                return task.getWorkDone() + children.stream().mapToDouble(Worker::getWorkDone).sum();
            }
        };

        DoubleBinding relativeProgress = new DoubleBinding() {
            {
                bind(totalProgress, progress);
            }

            @Override
            protected double computeValue() {
                return 0;
            }
        };
        progressProperty.bind(progress);
        totalProgressProperty.bind(totalProgress);
        relativeProgressProperty.bind(relativeProgress);
    }

    public TaskWorker(Worker<V> task, Collection<Worker<?>> children) {
        this(task);
        this.children.addAll(children);
    }

    @Override
    public ObservableList<Worker<?>> getChildren() {
        return children;
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return task.stateProperty();
    }

    @Override
    public ReadOnlyObjectProperty<V> valueProperty() {
        return task.valueProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return task.exceptionProperty();
    }

    @Override
    public ReadOnlyDoubleProperty workDoneProperty() {
        return progressProperty;
    }

    @Override
    public ReadOnlyDoubleProperty totalWorkProperty() {
        return totalProgressProperty;
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return relativeProgressProperty;
    }

    @Override
    public ReadOnlyBooleanProperty runningProperty() {
        return task.runningProperty();
    }

    @Override
    public ReadOnlyStringProperty messageProperty() {
        return task.messageProperty();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return task.titleProperty();
    }

    @Override
    public boolean cancel() {
        return task.cancel();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (task instanceof Future) {
            ((Future) task).cancel(mayInterruptIfRunning);
        }
        //Cancel children tasks
        children.forEach(child -> {
            if (child instanceof Future) {
                ((Future) child).cancel(mayInterruptIfRunning);
            }
        });
    }

    @Override
    public boolean isCancelled() {
        return task.getState() == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return task.getState() == State.SUCCEEDED || task.getState() == State.CANCELLED || task.getState() == State.FAILED;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return task.getValue();
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}

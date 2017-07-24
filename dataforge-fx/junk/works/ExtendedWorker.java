package hep.dataforge.fx.works;

import javafx.collections.ObservableList;
import javafx.concurrent.Worker;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

/**
 * An extension to JavaFX Worker to account for dependencies
 * @param <V>
 */
public interface ExtendedWorker<V> extends Worker<V>, Future<V> {

    @Override
    default State getState() {
        return stateProperty().get();
    }

    @Override
    default V getValue() {
        return valueProperty().get();
    }

    @Override
    default Throwable getException() {
        return exceptionProperty().get();
    }

    @Override
    default double getWorkDone() {
        return workDoneProperty().get();
    }

    @Override
    default double getTotalWork() {
        return totalWorkProperty().get();
    }

    @Override
    default double getProgress() {
        return progressProperty().get();
    }

    @Override
    default boolean isRunning() {
        return runningProperty().get();
    }

    @Override
    default String getMessage() {
        return messageProperty().get();
    }

    @Override
    default String getTitle() {
        return titleProperty().get();
    }

    /**
     * Get the list of subtasks
     * @return
     */
    ObservableList<Worker<?>> getChildren();
}

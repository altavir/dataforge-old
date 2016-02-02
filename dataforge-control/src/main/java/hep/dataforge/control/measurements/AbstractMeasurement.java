/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.utils.ReferenceRegistry;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.util.Pair;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractMeasurement<T> implements Measurement<T> {

    private final ReferenceRegistry<MeasurementListener<T>> listeners = new ReferenceRegistry<>();
    private Pair<T, Instant> lastResult;
    private Throwable exception;
    protected volatile boolean isFinished = false;

    private FutureTask<Pair<T, Instant>> task;

    /**
     * Call after measurement started
     */
    protected void notifyStarted() {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStarted(AbstractMeasurement.this));
    }

    /**
     * Call after measurement stopped
     */
    protected void notifyStopped() {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStopped(AbstractMeasurement.this));
    }

    protected void fail(Throwable error) {
        this.exception = error;
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFailed(AbstractMeasurement.this, error));
    }

    protected void result(T result) {
        result(result, Instant.now());
    }

    protected synchronized void result(T result, Instant time) {
        this.lastResult = new Pair<>(result, time);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementResult(AbstractMeasurement.this, result, time));
    }

    protected void progressUpdate(double progress) {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementProgress(AbstractMeasurement.this, progress));
    }

    protected void progressUpdate(String message) {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementProgress(AbstractMeasurement.this, message));
    }

    @Override
    public void addListener(MeasurementListener<T> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(MeasurementListener<T> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Instant getTime() throws MeasurementException {
        return get().getValue();
    }

    @Override
    public T getResult() throws MeasurementException {
        return get().getKey();
    }

    @Override
    public Throwable getError() {
        return this.exception;
    }

    private Pair<T, Instant> get() throws MeasurementException {
        if (this.lastResult != null) {
            return this.lastResult;
        } else {
            try {
                return getTask().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new MeasurementException(exception);
            }
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    protected FutureTask<Pair<T, Instant>> buildTask() {
        return new FutureTask<>(() -> {
            T res = doMeasure();
            Instant time = Instant.now();
            result(res, time);
            reset();
            clearTask();
            return new Pair<>(res, time);
        });

    }

    /**
     * invalidate current task. New task will be created on next getTask call.
     * This method does not guarantee that task is finished when it is cleared
     */
    protected final void clearTask() {
        task = null;
    }

    /**
     * Perform synchronous measurement
     *
     * @return
     * @throws Exception
     */
    protected abstract T doMeasure() throws Exception;

    @Override
    public void start() {
        getTask().run();
        notifyStarted();
    }

    @Override
    public boolean stop(boolean force) {
        if (getTask().cancel(force)) {
            notifyStopped();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reset task after single measurement is complete. This method should be
     * used to restart task for recurrent measurements.
     */
    protected void reset() {
        isFinished = true;
    }

    protected FutureTask<Pair<T, Instant>> getTask() {
        if (task == null) {
            task = buildTask();
        }
        return task;
    }

}

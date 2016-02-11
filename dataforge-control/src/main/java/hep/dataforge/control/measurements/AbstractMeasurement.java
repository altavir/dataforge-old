/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.utils.ReferenceRegistry;
import java.time.Instant;
import javafx.util.Pair;

/**
 * A boilerplate code for measurements
 *
 * @author Alexander Nozik
 */
public abstract class AbstractMeasurement<T> implements Measurement<T> {

    protected final ReferenceRegistry<MeasurementListener<T>> listeners = new ReferenceRegistry<>();
    protected Pair<T, Instant> lastResult;
    protected Throwable exception;
    protected volatile boolean isFinished = false;
    protected volatile boolean isStarted = false;

    /**
     * Call after measurement started
     */
    protected void onStart() {
        isStarted = true;
        isFinished = false;
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStarted(this));
    }

    /**
     * Call after measurement stopped
     */
    protected void onStop() {
        isFinished = true;
        isStarted = false;
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStopped(this));
    }

    protected void onError(Throwable error) {
        this.exception = error;
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFailed(this, error));
    }

    protected void onResult(T result) {
        result(result, Instant.now());
    }

    protected synchronized void result(T result, Instant time) {
        this.lastResult = new Pair<>(result, time);
        notify();
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementResult(this, result, time));
    }

    protected void onProgressUpdate(double progress) {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementProgress(this, progress));
    }

    protected void onProgressUpdate(String message) {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementProgress(this, message));
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
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public boolean isIsStarted() {
        return isStarted;
    }

    @Override
    public Throwable getError() {
        return this.exception;
    }

    protected synchronized Pair<T, Instant> get() throws MeasurementException {
        if (!isStarted) {
            start();
        }
        while (this.lastResult == null) {
            try {
                //Wait for onResult could cause deadlock if called in main thread
                wait();
            } catch (InterruptedException ex) {
                throw new MeasurementException(exception);
            }
        }
        return this.lastResult;
    }

    @Override
    public Instant getTime() throws MeasurementException {
        return get().getValue();
    }

    @Override
    public T getResult() throws MeasurementException {
        return get().getKey();
    }

}

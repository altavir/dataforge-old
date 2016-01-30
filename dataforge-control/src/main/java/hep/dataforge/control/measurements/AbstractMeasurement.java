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
import java.util.concurrent.Future;
import javafx.util.Pair;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractMeasurement<T> implements Measurement<T> {

    private final ReferenceRegistry<MeasurementListener<T>> listeners = new ReferenceRegistry<>();
    private Pair<T, Instant> lastResult;
    private Throwable exception;


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
        this.lastResult = new Pair<>(result,time);
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

    @Override
    public Pair<T, Instant> get() throws MeasurementException {
        if(this.lastResult != null){
            return this.lastResult;
        } else {
            try {
                return measurementTask().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new MeasurementException(exception);
            }
        }
    }

    
    protected abstract Future<Pair<T, Instant>> measurementTask();


}

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
import org.slf4j.LoggerFactory;

/**
 * A boilerplate code for measurements
 *
 * @author Alexander Nozik
 */
public abstract class AbstractMeasurement<T> implements Measurement<T> {

    protected enum MeasurementState {
        INIT, //Measurement not started
        PENDING, // Measurement in process
        OK, // Last measurement complete, the same as FINISHED for one-time measurements
        FAILED, // Last measurement failed 
        FINISHED, // Measurement finished or stopped
    }

    private MeasurementState state;
    protected final ReferenceRegistry<MeasurementListener<T>> listeners = new ReferenceRegistry<>();
    protected Pair<T, Instant> lastResult;
    protected Throwable exception;

    protected MeasurementState getState() {
        return state;
    }

    protected void setState(MeasurementState state) {
        this.state = state;
    }

    /**
     * Call after measurement started
     */
    protected void onStart() {
        setState(MeasurementState.PENDING);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStarted(this));
    }

    /**
     * Call after measurement stopped
     */
    protected void onFinish() {
        setState(MeasurementState.FINISHED);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFinished(this));
    }

    protected synchronized void onError(Throwable error) {
        LoggerFactory.getLogger(getClass()).error("Measurement failed with error", error);
        setState(MeasurementState.FAILED);
        this.exception = error;
        notify();
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFailed(this, error));
    }

    protected void onResult(T result) {
        result(result, Instant.now());
    }

    protected synchronized void result(T result, Instant time) {
        this.lastResult = new Pair<>(result, time);
        setState(MeasurementState.OK);
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
        return state == MeasurementState.FINISHED;
    }

    @Override
    public boolean isStarted() {
        return state == MeasurementState.PENDING || state == MeasurementState.OK;
    }

    @Override
    public Throwable getError() {
        return this.exception;
    }

    protected synchronized Pair<T, Instant> get() throws MeasurementException {
        if (getState() == MeasurementState.INIT) {
            start();
            LoggerFactory.getLogger(getClass()).debug("Measurement not started. Starting");
        }
        while (state == MeasurementState.PENDING) {
            try {
                //Wait for onResult could cause deadlock if called in main thread
                wait();
            } catch (InterruptedException ex) {
                throw new MeasurementException(ex);
            }
        }
        if(this.lastResult != null){
            return this.lastResult;
        } else if(state == MeasurementState.FAILED) {
            throw new MeasurementException(getError());
        } else {
            throw new MeasurementException("Measurement failed for unknown reason");
        }
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

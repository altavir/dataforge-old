/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.utils.ReferenceRegistry;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * A boilerplate code for measurements
 *
 * @author Alexander Nozik
 */
public abstract class AbstractMeasurement<T> implements Measurement<T> {

    protected final ReferenceRegistry<MeasurementListener<T>> listeners = new ReferenceRegistry<>();
    protected Pair<T, Instant> lastResult;
    protected Throwable exception;
    private MeasurementState state;

    protected MeasurementState getMeasurementState() {
        return state;
    }

    protected void setMeasurementState(MeasurementState state) {
        this.state = state;
    }

    /**
     * Call after measurement started
     */
    protected void afterStart() {
        setMeasurementState(MeasurementState.PENDING);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementStarted(this));
    }

    /**
     * Call after measurement stopped
     */
    protected void afterStop() {
        setMeasurementState(MeasurementState.FINISHED);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFinished(this));
    }

    /**
     * Reset measurement to initial state
     */
    protected void afterPause() {
        setMeasurementState(MeasurementState.OK);
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFinished(this));
    }

    protected synchronized void error(Throwable error) {
        LoggerFactory.getLogger(getClass()).error("Measurement failed with error", error);
        setMeasurementState(MeasurementState.FAILED);
        this.exception = error;
        notify();
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementFailed(this, error));
    }

    /**
     * Internal method to notify measurement complete. Uses current system time
     *
     * @param result
     */
    protected final void result(T result) {
        result(result, Instant.now());
    }

    /**
     * Internal method to notify measurement complete
     *
     * @param result
     */
    protected synchronized void result(T result, Instant time) {
        this.lastResult = new Pair<>(result, time);
        setMeasurementState(MeasurementState.OK);
        notify();
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementResult(this, result, time));
    }

    protected void progressUpdate(double progress) {
        listeners.forEach((MeasurementListener<T> t) -> t.onMeasurementProgress(this, progress));
    }

    protected void progressUpdate(String message) {
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
        if (getMeasurementState() == MeasurementState.INIT) {
            start();
            LoggerFactory.getLogger(getClass()).debug("Measurement not started. Starting");
        }
        while (state == MeasurementState.PENDING) {
            try {
                //Wait for result could cause deadlock if called in main thread
                wait();
            } catch (InterruptedException ex) {
                throw new MeasurementException(ex);
            }
        }
        if (this.lastResult != null) {
            return this.lastResult;
        } else if (state == MeasurementState.FAILED) {
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

    protected enum MeasurementState {
        INIT, //Measurement not started
        PENDING, // Measurement in process
        OK, // Last measurement complete, next is planned
        FAILED, // Last measurement failed
        FINISHED, // Measurement finished or stopped
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.exceptions.MeasurementException;
import java.time.Instant;

/**
 * A general representation of ongoing or completed measurement. Could be
 * regular. 
 *
 * @author Alexander Nozik
 */
public interface Measurement<T> {

    /**
     * Begin the measurement
     *
     * @throws MeasurementException
     */
    void start();

    /**
     * Stop the measurement
     *
     * @param force force stop if measurement in progress
     * @throws MeasurementException
     */
    boolean stop(boolean force)throws MeasurementException;

    /**
     * Measurement is started
     * @return 
     */
    public boolean isStarted();
    
    /**
     * Measurement is complete or stopped
     *
     * @return
     */
    boolean isFinished();

    /**
     * Get the time of the last measurement
     *
     * @return
     * @throws MeasurementException
     */
    Instant getTime() throws MeasurementException;

    /**
     * Get last measurement result or wait for measurement to complete and
     * return its result. Synchronous call.
     *
     * @return
     * @throws MeasurementException
     */
    T getResult() throws MeasurementException;
    
    /**
     * Last thrown exception. Null if no exceptions are thrown
     * @return 
     */
    Throwable getError();

    public void addListener(MeasurementListener<T> listener);

    public void removeListener(MeasurementListener<T> listener);
}
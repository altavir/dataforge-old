/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.control.devices.AbstractDevice;
import hep.dataforge.control.devices.StateDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.values.Value;

/**
 * A device with single one-time or periodic measurement
 *
 * @param <T>
 * @author Alexander Nozik
 */
@StateDef(name = "inProgress", info = "Shows if this sensor is actively measuring")
public abstract class Sensor<T> extends AbstractDevice {

    private Measurement<T> measurement;

    /**
     * Read sensor data synchronously
     *
     * @return
     * @throws hep.dataforge.exceptions.MeasurementException
     */
    public synchronized T read() throws MeasurementException {
        return startMeasurement().getResult();
    }

    public Measurement<T> startMeasurement() throws MeasurementException {
        if(!getState(INITIALIZED_STATE).booleanValue()){
            throw new RuntimeException("Device not initialized");
        }
        if (this.measurement == null || this.measurement.isFinished()) {
            this.measurement = createMeasurement();
//            measurement.addListener(this);
//            onCreateMeasurement(measurement);
        } else if (measurement.isStarted()) {
            getLogger().warn("Trying to start next measurement on sensor while previous measurement is active. Ignoring.");
        }

        this.measurement.start();
        return this.measurement;
    }

    public Measurement<T> getMeasurement() {
        return measurement;
    }

    /**
     * Stop current measurement
     *
     * @param force if true than current measurement will be interrupted even if
     *              running
     * @throws hep.dataforge.exceptions.MeasurementException
     */
    public void stopMeasurement(boolean force) throws MeasurementException {
        if (this.measurement != null && !this.measurement.isFinished()) {
            this.measurement.stop(force);
//            measurement.removeListener(this);
        }
    }

//    @Override
//    protected <T> void onFinishMeasurement(Measurement<T> measurement) {
//        measurement = null;
//    }

    @Override
    public void shutdown() throws ControlException {
        stopMeasurement(true);
        super.shutdown();
    }

    /**
     * Shows if there is ongoing measurement
     *
     * @return
     */
    public boolean isMeasuring() {
        return measurement != null && !measurement.isFinished();
    }

    @Override
    public Value getState(String stateName) {
        if ("measuring".equals(stateName)) {
            return Value.of(isMeasuring());
        } else {
            return super.getState(stateName);
        }
    }

    protected abstract Measurement<T> createMeasurement() throws MeasurementException;

}

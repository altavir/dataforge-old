/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.control.RoleDef;
import hep.dataforge.control.connections.Roles;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.MeasurementListener;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;

import static hep.dataforge.control.devices.Sensor.MEASURING_STATE;

/**
 * A device with single one-time or periodic measurement
 *
 * @param <T>
 * @author Alexander Nozik
 */
@StateDef(name = MEASURING_STATE, info = "Shows if this sensor is actively measuring")
@RoleDef(name = Roles.MEASUREMENT_LISTENER_ROLE, objectType = MeasurementListener.class)
public abstract class Sensor<T> extends AbstractDevice {
    public static final String MEASURING_STATE = "measuring";

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
        if (!getState(INITIALIZED_STATE).booleanValue()) {
            throw new RuntimeException("Device not initialized");
        }
        if (this.measurement == null || this.measurement.isFinished()) {
            this.measurement = createMeasurement();
        } else if (measurement.isStarted()) {
            getLogger().warn("Trying to start next measurement on sensor while previous measurement is active. Ignoring.");
        }

        this.measurement.start();
        updateState(MEASURING_STATE, true);
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
            updateState(MEASURING_STATE, false);
        }
    }

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
    protected Object computeState(String stateName) throws ControlException {
        if (MEASURING_STATE.equals(stateName)) {
            return isMeasuring();
        } else {
            return super.computeState(stateName);
        }
    }

    protected abstract Measurement<T> createMeasurement() throws MeasurementException;

}

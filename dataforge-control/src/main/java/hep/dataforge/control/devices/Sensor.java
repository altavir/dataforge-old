/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.context.Context;
import hep.dataforge.control.RoleDef;
import hep.dataforge.control.connections.Roles;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.MeasurementListener;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

import java.util.Objects;

import static hep.dataforge.control.devices.Sensor.MEASURING_STATE;

/**
 * A device with single one-time or periodic measurement
 *
 * @param <T>
 * @author Alexander Nozik
 */
@StateDef(
        value = @ValueDef(name = MEASURING_STATE, info = "Shows if this sensor is actively measuring"),
        writable = true
)
@RoleDef(name = Roles.MEASUREMENT_LISTENER_ROLE, objectType = MeasurementListener.class)
public abstract class Sensor<T> extends AbstractDevice {
    public static final String MEASURING_STATE = "measuring";

    private Measurement<T> measurement;

    public Sensor(Context context, Meta meta) {
        super(context, meta);
    }

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
        updateLogicalState(MEASURING_STATE, true);
        return this.measurement;
    }

    @Override
    protected void requestStateChange(String stateName, Value value) throws ControlException {
        if (Objects.equals(stateName, MEASURING_STATE)) {
            if (value.booleanValue()) {
                startMeasurement();
            } else {
                stopMeasurement(false);
            }
        }
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
            updateLogicalState(MEASURING_STATE, false);
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
            return getLogicalState(stateName);
        }
    }

    protected abstract Measurement<T> createMeasurement() throws MeasurementException;

}

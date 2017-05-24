/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

import static hep.dataforge.control.devices.SingleMeasurementDevice.MEASURING_STATE;

/**
 * A device that allows different types of measurements, but only one active at
 * a time
 *
 * @author Alexander Nozik
 */
@StateDef(name = MEASURING_STATE, info = "True if measurement is currently in progress")
public abstract class SingleMeasurementDevice<T extends Measurement> extends AbstractDevice {

    public static final String MEASURING_STATE = "measuring";

    private T measurement;

    public T getMeasurement() {
        return measurement;
    }

    @Override
    public Value getState(String stateName) {
        if (MEASURING_STATE.equals(stateName)) {
            return Value.of(this.measurement != null && ! this.measurement.isFinished());
        } else {
            return super.getState(stateName);
        }
    }

    public T startMeasurement() throws ControlException {
        return startMeasurement(getMeasurementMeta());
    }

    public T startMeasurement(Meta meta) throws ControlException {
        this.measurement = createMeasurement(meta);
        recalculateState(MEASURING_STATE);
        this.measurement.start();
        return this.measurement;
    }

    /**
     * Stop current measurement
     *
     * @param force if true than current measurement will be interrupted even if
     *              running
     */
    public void stopMeasurement(boolean force) throws MeasurementException {
        if (this.measurement != null && !this.measurement.isFinished()) {
            this.measurement.stop(force);
        }
    }

    protected abstract T createMeasurement(Meta meta) throws ControlException;

    /**
     * Compute default meta for measurement
     *
     * @return
     */
    protected Meta getMeasurementMeta() {
        return meta().getMetaOrEmpty("measurement");
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import static hep.dataforge.control.devices.SingleMeasurementDevice.MEASURING_STATE;
import hep.dataforge.control.devices.annotations.StateDef;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * A device that allows different types of measurements, but only one active at
 * a time
 *
 * @author Alexander Nozik
 */
@StateDef(name = MEASURING_STATE, info = "True if measurement is currently in progress")
public abstract class SingleMeasurementDevice<T extends Measurement> extends AbstractMeasurementDevice {

    public static final String MEASURING_STATE = "measuring";

    private T measurement;

//    public SingleMeasurementDevice(String name, Context context, Meta meta) {
//        super(name, context, meta);
//    }
    public T getMeasurement() {
        return measurement;
    }

    @Override
    public Value getState(String stateName) {
        if (MEASURING_STATE.equals(stateName)) {
            return Value.of(this.measurement != null);
        } else {
            return super.getState(stateName);
        }
    }

    public T startMeasurement(String type) throws ControlException {
        return startMeasurement(getMetaForMeasurement(type));
    }

    public T startMeasurement(Meta meta) throws ControlException {
        this.measurement = createMeasurement(meta);
        onCreateMeasurement(measurement);
        measurement.addListener(this);
        recalculateState(MEASURING_STATE);
        this.measurement.start();
        return this.measurement;
    }

    /**
     * Stop current measurement
     *
     * @param force if true than current measurement will be interrupted even if
     * running
     */
    public void stopMeasurement(boolean force) throws MeasurementException {
        if (this.measurement != null && !this.measurement.isFinished()) {
            this.measurement.stop(force);
        }
    }

    @Override
    public void onMeasurementFinished(Measurement measurement) {
        this.measurement = null;
        recalculateState(MEASURING_STATE);
    }
    

    protected abstract T createMeasurement(Meta meta) throws ControlException;

}

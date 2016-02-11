/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.context.Context;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;

/**
 * A device that allows different types of measurements, but only one active at
 * a time
 *
 * @author Alexander Nozik
 */
public abstract class SingleMeasurementDevice<T extends Measurement> extends AbstractMeasurementDevice {

    private T measurement;

    public SingleMeasurementDevice(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    public T getMeasurement() {
        return measurement;
    }

    public T startMeasurement(String type) throws ControlException {
        return startMeasurement(getMetaForMeasurement(type));
    }

    public T startMeasurement(Meta meta) throws ControlException {
        this.measurement = createMeasurement(meta);
        onCreateMeasurement(measurement);
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

    protected abstract T createMeasurement(Meta meta) throws ControlException;

}

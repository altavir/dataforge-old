/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.context.Context;
import hep.dataforge.control.devices.AbstractMeasurementDevice;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * A device with single one-time or periodic measurement
 *
 * @author Alexander Nozik
 */
public abstract class Sensor<T> extends AbstractMeasurementDevice {
    
    private Measurement<T> measurement;
    
    public Sensor(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    /**
     * Read sensor data synchronously
     *
     * @return
     */
    public T read() throws MeasurementException {
        return startMeasurement().getResult();
    }
    
    public Measurement<T> startMeasurement() throws MeasurementException {
        if (this.measurement == null || this.measurement.isFinished()) {
            this.measurement = createMeasurement();
            onCreateMeasurement(measurement);
            this.measurement.start();
        } else {
            getLogger().warn("Trying to start next measurement on sensor while previous measurement is active. Ignoring.");
        }
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
    protected Object calculateState(String stateName) throws ControlException {
        return Value.NULL;
    }
    
    
    
    protected abstract Measurement<T> createMeasurement() throws MeasurementException;
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.context.Context;
import hep.dataforge.control.devices.AbstractDevice;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;

/**
 * A device with single one-time or periodic measurement
 * @author Alexander Nozik
 */
public abstract class SingleMeasurementDevice<T extends Measurement> extends AbstractDevice {
    private T measurement;

    public SingleMeasurementDevice(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    @Override
    protected void evalCommand(String command, Meta commandMeta) throws ControlException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public T getMeasurement(){
        if(this.measurement == null || this.measurement.isFinished()){
            this.measurement = createMeasurement();
        } 
        return measurement;
    }
    
    protected abstract T createMeasurement();
    
}

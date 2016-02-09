/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.context.Context;
import hep.dataforge.control.devices.AbstractDevice;
import hep.dataforge.exceptions.MeasurementException;
import hep.dataforge.meta.Meta;

/**
 * A device with single one-time or periodic measurement
 * @author Alexander Nozik
 */
public abstract class Sensor<T> extends AbstractDevice {
    private Measurement<T> measurement;

    public Sensor(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

//    @Override
//    protected void evalCommand(String command, Meta commandMeta) throws ControlException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    public Measurement<T> getMeasurement(){
        if(this.measurement == null || this.measurement.isFinished()){
            this.measurement = createMeasurement();
        } 
        return measurement;
    }
    
    /**
     * Read sensor data synchronously
     * @return 
     */
    public T read() throws MeasurementException{
        return getMeasurement().getResult();
    }
    
    public Measurement<T> startMeasurement(){
        getMeasurement().start();
        return measurement;
    }
    
    protected abstract Measurement<T> createMeasurement();
    
}

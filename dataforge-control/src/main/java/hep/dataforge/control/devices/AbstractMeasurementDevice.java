/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.context.Context;
import hep.dataforge.control.connections.MeasurementListenerFactory;
import hep.dataforge.control.connections.Roles;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.meta.Meta;


public abstract class AbstractMeasurementDevice extends AbstractDevice {

    public AbstractMeasurementDevice(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    protected <T> void startMeasurement(Measurement<T> measurement){
        this.forEachTypedConnection(Roles.MEASUREMENT_LISTENER_ROLE, MeasurementListenerFactory.class,
                (MeasurementListenerFactory con)-> measurement.addListener(con.getListener(measurement, resultType)));
    }
}

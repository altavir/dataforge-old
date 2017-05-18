/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.control.RoleDef;
import hep.dataforge.control.connections.MeasurementConsumer;
import hep.dataforge.control.connections.Roles;
import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.control.measurements.MeasurementListener;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;

import java.time.Instant;

@RoleDef(name = Roles.MEASUREMENT_CONSUMER_ROLE,objectType = MeasurementConsumer.class, info = "Listen to measurement start events")
public abstract class AbstractMeasurementDevice<T> extends AbstractDevice implements MeasurementListener<T> {

    /**
     * The method that must be called after measurement is started
     *
     * @param <T>
     * @param measurement
     */
    protected <T> void onCreateMeasurement(Measurement<T> measurement) {
        forEachConnection(Roles.MEASUREMENT_CONSUMER_ROLE, MeasurementConsumer.class,
                (MeasurementConsumer t) -> t.accept(AbstractMeasurementDevice.this, measurement));
    }

    protected <T> void onCreateMeasurement(String measurementName, Measurement<T> measurement) {
        forEachConnection(Roles.MEASUREMENT_CONSUMER_ROLE, MeasurementConsumer.class,
                (MeasurementConsumer t) -> t.accept(AbstractMeasurementDevice.this, measurementName, measurement));
    }
    

    @Override
    public void onMeasurementResult(Measurement<T> measurement, T result, Instant time) {
        
    }

    @Override
    public void onMeasurementFailed(Measurement<T> measurement, Throwable exception) {
        notifyError("Measurement failed", exception);
    }

    
    
    /**
     * Compute default meta for measurement
     *
     * @param name
     * @return
     */
    protected Meta getMetaForMeasurement(String name) {
        return MetaUtils.findNodeByValue(meta(), "measurement", "name", name);
    }
}

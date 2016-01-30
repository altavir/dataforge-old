/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.control.measurements;

import hep.dataforge.content.AnonimousNotAlowed;
import hep.dataforge.context.Context;
import hep.dataforge.control.devices.AbstractDevice;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.Map;

@AnonimousNotAlowed
public abstract class AbstractMeasurementDevice<T> extends AbstractDevice implements MeasurementDevice {

    private final Map<String, Measurement> measurements = new HashMap<>();

    public AbstractMeasurementDevice(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    @Override
    public Measurement createMeasurement(String name, Meta meta) throws ControlException {
        if(meta == null){
            meta = getMetaForMeasurement(name);
        }
        Measurement m = doCreateMeasurement(name, meta);
        measurements.put(name, m);
        return m;
    }
    
    protected abstract Measurement doCreateMeasurement(String name, Meta meta);

    public Measurement createMeasurement(String name) throws ControlException {
        return createMeasurement(name, getMetaForMeasurement(name));
    }

    @Override
    public  Measurement getMeasurement(String name) {
        return this.measurements.get(name);
    }

    /**
     * Compute default meta for measurement
     *
     * @param name
     * @return
     */
    protected Meta getMetaForMeasurement(String name) {
        return Meta.buildEmpty("measurement");
    }

    /**
     * Clean up old measurements
     */
    protected void cleanup() {
        for (Map.Entry<String, Measurement> entry : measurements.entrySet()) {
            if (entry.getValue().isFinished()) {
                measurements.remove(entry.getKey());
            }
        }
    }

//    private final ReferenceRegistry<MeasurementListener<T>> measurementListeners = new ReferenceRegistry<>();
//
//    public AbstractMeasurementDevice(String name, Context context, Meta annotation) {
//        super(name, context, annotation);
//    }
//
//    protected Meta getDefaultMeasurement() {
//        return Meta.buildEmpty("measurement");
//    }
//
//    /**
//     * Build a laminate using provided measurement meta and device configuration
//     * as well as context values.
//     *
//     * @param measurement
//     * @return
//     */
//    protected Meta buildMeasurementLaminate(Meta measurement) {
//        return new Laminate("measurement", measurement, meta()).setDefaultValueProvider(context);
//    }
//
//    @Override
//    public void start() throws ControlException {
//        start(getDefaultMeasurement());
//    }
//
//    @Override
//    public final void start(Meta measurement) throws ControlException {
//        doStart(measurement);
//        measurementListeners.forEach(it -> it.notifyMeasurementStarted(this, measurement));
//    }
//
//    protected abstract void doStart(Meta measurement) throws ControlException;
//
//    @Override
//    public final void stop() throws ControlException {
//        doStop();
//        measurementListeners.forEach(it -> it.notifyMeasurementStopped(this));
//    }
//
//    protected abstract void doStop() throws ControlException;
//
//    @Override
//    public void addMeasurementListener(MeasurementListener<T> listener) {
//        measurementListeners.add(listener);
//    }
//
//    protected final void measurementResult(Meta measurement, T measurementResult) {
//        getLogger().debug("Notify measurement complete");
//        measurementListeners.forEach(it -> it.notifyMeasurementResult(this, measurement, measurementResult));
//    }
//
//    @Override
//    public void removeMeasurementListener(MeasurementListener<T> listener) {
//        measurementListeners.remove(listener);
//    }
}

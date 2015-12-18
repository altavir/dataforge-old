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
package hep.dataforge.control.devices;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;

/**
 * A listener for device measurements
 * @author Alexander Nozik
 */
public interface MeasurementListener<T> {

    /**
     * Notify listener that measurement with given meta is started
     * @param device
     * @param measurement 
     */
    void notifyMeasurementStarted(MeasurementDevice device, Meta measurement);
    
    /**
     * In case we will need binary data passed to device
     * @param device
     * @param measurement 
     */
    default void notifyMeasurementStarted(MeasurementDevice device, Envelope measurement){
        //Notify measurement started avoiding binary part of the instruction envelope
        notifyMeasurementStarted(device, measurement.meta());
    }
    
    void notifyMeasurementStopped(MeasurementDevice device);

    void notifyMeasurementResult(MeasurementDevice device, Meta measurement, T measurementResult);
}

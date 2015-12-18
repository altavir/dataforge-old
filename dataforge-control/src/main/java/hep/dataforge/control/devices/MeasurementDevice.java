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

import hep.dataforge.exceptions.ControlException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;

/**
 * An interface for devices which provide measurements
 *
 * @author darksnake
 */
public interface MeasurementDevice<T> extends Device {

    /**
     * Start or power-up device and begin measurement. If called again with
     * different measurement stops current measurement (with or without powering
     * down depending on device) and starts new one.
     *
     * @throws ControlException
     */
    void start(Meta measurement) throws ControlException;
    
    /**
     * Start measurement with complex instruction that can include binary data
     *
     * @throws ControlException
     */
    default void start(Envelope instruction) throws ControlException{
        start(instruction.meta());
    }    

    /**
     * Start default measurement
     *
     * @throws ControlException
     */
    void start() throws ControlException;

    /**
     * Stop (power down) device. Does nothing if device is not started.
     *
     * @throws ControlException
     */
    void stop() throws ControlException;

    void addMeasurementListener(MeasurementListener<T> listener);
    void removeMeasurementListener(MeasurementListener<T> listener);
}

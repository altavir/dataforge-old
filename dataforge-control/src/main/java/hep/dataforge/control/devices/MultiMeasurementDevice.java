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

import hep.dataforge.control.measurements.Measurement;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;

/**
 * An interface for devices which provide measurements
 *
 * @author darksnake
 */
public interface MultiMeasurementDevice extends Device {
    
    Measurement createMeasurement(String name, Meta meta) throws ControlException;
    
    Measurement getMeasurement(String name);
    
    default Measurement startMeasurement(String name) throws ControlException{
        Measurement measurement = getMeasurement(name);
        if(measurement == null){
            measurement = createMeasurement(name, null);
        }
        measurement.start();
        return measurement;
    }
}

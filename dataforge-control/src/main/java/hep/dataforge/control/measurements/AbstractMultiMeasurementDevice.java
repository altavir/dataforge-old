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

/**
 * The device that allows multiple different measurements simultaneously
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public abstract class AbstractMultiMeasurementDevice extends AbstractDevice implements MultiMeasurementDevice {

    private final Map<String, Measurement> measurements = new HashMap<>();

    public AbstractMultiMeasurementDevice(String name, Context context, Meta meta) {
        super(name, context, meta);
    }

    @Override
    public Measurement createMeasurement(String name, Meta meta) throws ControlException {
        if (meta == null) {
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
    public Measurement getMeasurement(String name) {
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

}

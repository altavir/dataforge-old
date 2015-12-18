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

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.StoragePlugin;

/**
 * The device that can store data in the storage. All needed loaders should be
 * initialized during {@code start} call using provided measurement
 * configuration.
 *
 * @author darksnake
 */
public abstract class DataDevice<T> extends AbstractMeasurementDevice<T> {

//    Storage primaryStorage;
//    Storage secondaryStorage;
//    EventLoader eventLoader;
    public DataDevice(String name, Context context, Meta annotation) {
        super(name, context, annotation);
        //Load storage plugin locally if it is not loaded
        if (!context.provides("storage")) {
            context.loadPlugin(new StoragePlugin());
        }
    }

    /**
     * Construct primary (local) storage for this device and measurement.
     * Instruction configuration overrides device configuration.
     *
     * @param measurement
     * @return
     */
    protected Storage getPrimaryStorage(Meta measurement) {
        Storage primaryStorage;
        Meta meta = buildMeasurementLaminate(measurement);

        StoragePlugin sp = getContext().provide("storage", StoragePlugin.class);

        if (meta.hasNode("storage.local")) {
            primaryStorage = sp.buildStorage(meta.getNode("storage.local"));
        } else if (meta.hasNode("storage.primary")) {
            primaryStorage = sp.buildStorage(meta.getNode("storage.primary"));
        } else if (meta.hasNode("storage")) {
            primaryStorage = sp.buildStorage(meta.getNode("storage"));
        } else {
            primaryStorage = sp.getDefaultStorage();
        }

        return primaryStorage;
    }

    /**
     * Construct secondary (remote) storage for this device and measurement.
     * Instruction configuration overrides device configuration. If appropriate
     * element is not defined then null is returned.
     *
     * @return
     */
    protected Storage getSecondaryStorage(Meta measurement) {
        Storage secondaryStorage;
        Meta meta = buildMeasurementLaminate(measurement);

        StoragePlugin sp = getContext().provide("storage", StoragePlugin.class);

        if (meta.hasNode("storage.remote")) {
            secondaryStorage = sp.buildStorage(meta.getNode("storage.remote"));
        } else if (meta.hasNode("storage.secondary")) {
            secondaryStorage = sp.buildStorage(meta.getNode("storage.secondary"));
        } else {
            secondaryStorage = null;
        }

        return secondaryStorage;
    }

//    /**
//     * default event loader for this device
//     *
//     * @return
//     */
//    public EventLoader getEventLoader() throws StorageException {
//        return LoaderFactory.buildEventLoder(getPrimaryStorage(), getName(), getCurrentMeasurement().shelfName());
//    }
}

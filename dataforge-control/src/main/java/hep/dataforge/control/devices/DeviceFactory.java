package hep.dataforge.control.devices;

import hep.dataforge.utils.ContextMetaFactory;

/**
 * Created by darksnake on 06-May-17.
 */
public interface DeviceFactory<D extends Device> extends ContextMetaFactory<D> {
    /**
     * The type of the device factory. One factory can supply multiple device classes depending on configuration.
     *
     * @return
     */
    String getType();
}

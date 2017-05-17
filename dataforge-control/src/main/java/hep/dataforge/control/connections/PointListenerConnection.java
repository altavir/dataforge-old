package hep.dataforge.control.connections;

import hep.dataforge.control.Connection;
import hep.dataforge.control.devices.Device;
import hep.dataforge.tables.PointListener;

/**
 * A default extension of PointListener. Useful for PointLoaders and visualisation
 * Created by darksnake on 05-Oct-16.
 */
public interface PointListenerConnection extends PointListener, Connection<Device> {

    @Override
    default boolean isOpen() {
        return true;
    }

    @Override
    default void open(Device object) throws Exception {

    }

    @Override
    default void close() throws Exception {

    }
}

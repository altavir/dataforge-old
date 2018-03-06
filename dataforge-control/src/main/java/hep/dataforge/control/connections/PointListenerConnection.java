package hep.dataforge.control.connections;

import hep.dataforge.connections.Connection;
import hep.dataforge.tables.PointListener;

/**
 * A default extension of PointListener. Useful for PointLoaders and visualisation
 * Created by darksnake on 05-Oct-16.
 */
public interface PointListenerConnection extends PointListener, Connection {

    @Override
    default boolean isOpen() {
        return true;
    }

    @Override
    default void open(Object object) throws Exception {

    }

    @Override
    default void close() throws Exception {

    }

}

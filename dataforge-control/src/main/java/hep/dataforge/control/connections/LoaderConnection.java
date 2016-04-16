/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.devices.Device;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.points.DataPoint;
import hep.dataforge.points.PointListener;
import hep.dataforge.storage.api.PointLoader;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class LoaderConnection implements PointListener, Connection<Device> {

    private final PointLoader loader;

    public LoaderConnection(PointLoader loader) {
        this.loader = loader;
    }

    @Override
    public void accept(DataPoint point) {
        try {
            loader.push(point);
        } catch (StorageException ex) {
            LoggerFactory.getLogger(getClass()).error("Error while pushing data", ex);
        }
    }

    @Override
    public boolean isOpen() {
        return loader.isOpen();
    }

    @Override
    public void open(Device object) throws Exception {
        loader.open();
    }

    @Override
    public void close() throws Exception {
        loader.close();
    }

}

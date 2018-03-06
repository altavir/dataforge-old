/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.TableLoader;
import hep.dataforge.values.Values;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class LoaderConnection implements PointListenerConnection {

    private final TableLoader loader;

    public LoaderConnection(TableLoader loader) {
        this.loader = loader;
    }

    @Override
    public void accept(Values point) {
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
    public void open(Object object) {

    }

    @Override
    public void close() throws Exception {
        loader.close();
    }

}

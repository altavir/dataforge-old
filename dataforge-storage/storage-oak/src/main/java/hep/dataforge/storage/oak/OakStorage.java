/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.oak;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.AbstractStorage;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class OakStorage extends AbstractStorage {

    public OakStorage(Storage parent, String name, Meta annotation) {
        super(parent, name, annotation);
    }

    @Override
    protected Loader createLoader(String loaderName, Meta loaderConfiguration) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.    }
    }

    @Override
    protected Storage createShelf(Meta shelfConfiguration, String shelfName) throws StorageException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

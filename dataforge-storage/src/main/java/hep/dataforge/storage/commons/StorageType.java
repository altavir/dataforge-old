/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.storage.api.Storage;
import hep.dataforge.utils.MetaFactory;

/**
 * The type of storage with meta builder
 * @author Alexander Nozik
 */
public interface StorageType extends MetaFactory<Storage> {
    /**
     * The type of the storage
     * @return 
     */
    String type();
}

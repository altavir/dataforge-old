/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;

/**
 * The type of storage with meta builder
 *
 * @author Alexander Nozik
 */
public interface StorageType {

    /**
     * The type of the storage
     *
     * @return
     */
    String type();

    /**
     * Build storage
     * @param context
     * @param meta
     * @return 
     */
    Storage build(Context context, Meta meta);
}

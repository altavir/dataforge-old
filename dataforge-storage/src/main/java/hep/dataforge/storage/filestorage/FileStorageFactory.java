/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.StorageType;

/**
 *
 * @author Alexander Nozik
 */
public class FileStorageFactory implements StorageType {

    @Override
    public String type() {
        return "file";
    }

    @Override
    public Storage build(Context context, Meta annotation) {
        return FileStorage.from(context, annotation);
    }
    
}

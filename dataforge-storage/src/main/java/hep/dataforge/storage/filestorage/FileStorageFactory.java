/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.StorageType;

import java.io.File;

/**
 * @author Alexander Nozik
 */
public class FileStorageFactory implements StorageType {

    public static MetaBuilder buildStorageMeta(String path, boolean readOnly, boolean monitor) {
        return new MetaBuilder("storage")
                .setValue("path", path)
                .setValue("type", "file")
                .setValue("readOnly", readOnly)
                .setValue("monitor", monitor);
    }

    /**
     * Build local storage with Global context. Used for tests.
     * @param file
     * @return
     */
    public static FileStorage buildLocal(File file) {
        return new FileStorage(Global.instance(),
                new MetaBuilder("storage").setValue("path", file.getAbsolutePath()));
    }

    @Override
    public String type() {
        return "file";
    }

    @Override
    public Storage build(Context context, Meta annotation) {
        return new FileStorage(context, annotation);
    }

}

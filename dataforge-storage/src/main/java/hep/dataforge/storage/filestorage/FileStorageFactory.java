/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.StorageType;
import hep.dataforge.storage.commons.StorageManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Alexander Nozik
 */
public class FileStorageFactory implements StorageType {

    public static MetaBuilder buildStorageMeta(URI path, boolean readOnly, boolean monitor) {
        return new MetaBuilder("storage")
                .setValue("path", path.toString())
                .setValue("type", "file")
                .setValue("readOnly", readOnly)
                .setValue("monitor", monitor);
    }

    public static MetaBuilder buildStorageMeta(File file, boolean readOnly, boolean monitor) {
        return buildStorageMeta(file.toURI(), readOnly, monitor);
    }

    /**
     * Build local storage with Global context. Used for tests.
     *
     * @param file
     * @return
     */
    public static FileStorage buildLocal(Context context, File file, boolean readOnly, boolean monitor) {
        StorageManager manager = context.loadFeature("hep.dataforge:storage", StorageManager.class);
        return (FileStorage) manager.buildStorage(buildStorageMeta(file.toURI(),readOnly,monitor));
    }

    @Override
    public String type() {
        return "file";
    }

    @NotNull
    @Override
    public Storage build(Context context, Meta meta) {
        Path path = meta.optString("path").map(URI::create).map(Paths::get).orElse(context.getIo().getWorkDir());
        return new FileStorage(context, meta, path);
    }

}

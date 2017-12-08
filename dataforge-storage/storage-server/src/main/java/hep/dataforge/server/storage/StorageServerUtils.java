package hep.dataforge.server.storage;

import hep.dataforge.server.ServerManager;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorageFactory;

import java.io.File;

public class StorageServerUtils {

    public static void addStorage(ServerManager manager, Storage storage, String path) {
        storage.open();
        manager.bind(new StorageServerObject(manager, storage, path));
    }

    public static void addFileStorage(ServerManager manager, String uri, String path) {
        addStorage(
                manager,
                FileStorageFactory.buildLocal(manager.getContext(), new File(uri), false, true),
                path
        );
    }
}

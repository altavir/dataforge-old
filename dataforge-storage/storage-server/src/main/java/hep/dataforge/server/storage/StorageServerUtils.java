package hep.dataforge.server.storage;

import hep.dataforge.server.ServerManager;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;
import hep.dataforge.storage.filestorage.FileStorageFactory;

public class StorageServerUtils {

    public static void addStorage(ServerManager manager, Storage storage, String path) {
        storage.open();
        manager.bind(new StorageServerObject(manager, storage, path));
    }

    public static void addFileStorage(ServerManager manager, String uri, String path) {
        addStorage(
                manager,
                new FileStorage(manager.getContext(), FileStorageFactory.buildStorageMeta(uri, true, true)),
                path
        );
    }
}
package hep.dataforge.server.storage;

import hep.dataforge.server.ServerManager;
import hep.dataforge.server.ServerObject;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.Nullable;
import ratpack.handling.Chain;

import java.util.stream.Stream;

public class StorageServerObject implements ServerObject {

    private final ServerObject parent;
    private final ServerManager manager;
    private final Storage storage;
    private final String path;

    public StorageServerObject(ServerManager manager, Storage storage, String path) {
        this.manager = manager;
        this.storage = storage;
        parent = null;
        this.path = path;
    }

    public StorageServerObject(ServerObject parent, Storage storage) {
        this.manager = parent.getManager();
        this.storage = storage;
        this.parent = parent;
        path = storage.getName();
    }

    @Override
    public ServerManager getManager() {
        return manager;
    }

    @Override
    public @Nullable ServerObject getParent() {
        return parent;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void updateChain(Chain parentChain) {
        try {
            parentChain.prefix(getPath(), chain -> {
                //adding storage handler
                chain.get(new StorageRatpackHandler(manager, storage));
                //adding files (permissions?) if FileStorage
                if (storage instanceof FileStorage) {
                    FileObject dir = ((FileStorage) storage).getDataDir();
                    try {
                        chain.files(it -> it.dir(dir.getURL().getPath()));
                    } catch (Exception ex) {
                        getContext().getLogger().error("Failed to load storage chain", ex);
                    }
                }
                //Adding children chains
                ServerObject.super.updateChain(chain);
            });
        } catch (Exception e) {
            getContext().getLogger().error("Failed to load storage chain", e);
        }
    }

    @Override
    public Stream<ServerObject> getChildren() {
        return storage.shelves().stream().map(shelf -> new StorageServerObject(this, shelf));
    }
}

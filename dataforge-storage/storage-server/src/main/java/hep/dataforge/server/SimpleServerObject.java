package hep.dataforge.server;

import org.jetbrains.annotations.Nullable;
import ratpack.handling.Chain;

import java.util.function.Consumer;

public class SimpleServerObject implements ServerObject {
    private final ServerObject parent;
    private final String path;
    private final Consumer<Chain> chainUpdater;

    public SimpleServerObject(ServerObject parent, String path, Consumer<Chain> chainUpdater) {
        this.parent = parent;
        this.path = path;
        this.chainUpdater = chainUpdater;
    }

    @Override
    public ServerManager getManager() {
        return parent.getManager();
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
    public void updateChain(Chain chain) {
        chainUpdater.accept(chain);
    }
}

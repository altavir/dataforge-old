package hep.dataforge.server;

import hep.dataforge.context.Context;
import hep.dataforge.context.ContextAware;
import org.jetbrains.annotations.Nullable;
import ratpack.handling.Chain;

import java.util.stream.Stream;

/**
 *
 */
public interface ServerObject extends ContextAware, AutoCloseable {

    /**
     * Get server manager for this object
     *
     * @return
     */
    ServerManager getManager();

    @Nullable
    ServerObject getParent();

    /**
     * Path relative to parent
     *
     * @return
     */
    String getPath();

    /**
     * user-friendly name for this object
     * @return
     */
    default String getTitle(){
        return getPath();
    }


    /**
     * Get full path relative to server root
     */
    default String getFullPath() {
        return getParent() == null ? getPath() : getParent().getFullPath() + getPath();
    }

    /**
     * Update parent chain including prefix if needed
     *
     * @param chain
     */
    default void updateChain(Chain chain) {
        getChildren().forEach(child -> child.updateChain(chain));
    }

    default Stream<ServerObject> getChildren() {
        return Stream.empty();
    }

    @Override
    default Context getContext() {
        return getManager().getContext();
    }
}

package hep.dataforge.server;

/**
 * A server page factory for given type of objects
 * @param <T>
 */
public interface ServerWrapper<T> {

    Class<T> getType();

    ServerObject wrap(ServerObject parent, T object, String path);
}

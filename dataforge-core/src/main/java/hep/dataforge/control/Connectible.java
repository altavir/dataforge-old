/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control;

import hep.dataforge.kodex.CoreExtensionsKt;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Something that could be connected
 *
 * @author Alexander Nozik
 */
public interface Connectible {

    /**
     * Register a new connection with given roles
     *
     * @param connection
     * @param roles
     */
    void connect(Connection connection, String... roles);

    /**
     * Get a stream of all connections with given role and type. Role could be regexp
     *
     * @param role
     * @param type
     * @param <T>
     * @return
     */
    <T> Stream<T> connections(String role, Class<T> type);

    /**
     * Disconnect given connection
     *
     * @param connection
     */
    void disconnect(Connection connection);


    /**
     * For each connection of given class and role. Role may be empty, but type
     * is mandatory
     *
     * @param <T>
     * @param role
     * @param type
     * @param action
     */
    default <T> void forEachConnection(String role, Class<T> type, Consumer<T> action) {
        connections(role, type).forEach(action);
    }

    default <T> void forEachConnection(Class<T> type, Consumer<T> action) {
        forEachConnection(".*", type, action);
    }


    /**
     * A list of all available roles
     *
     * @return
     */
    default List<RoleDef> roleDefs() {
        return CoreExtensionsKt.listAnnotations(this.getClass(), RoleDef.class, true);
    }

    /**
     * Find a role definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default Optional<RoleDef> optRoleDef(String name) {
        return roleDefs().stream().filter((roleDef) -> roleDef.name().equals(name)).findFirst();
    }

    /**
     * A quick way to find if this object accepts connection with given role
     *
     * @param name
     * @return
     */
    default boolean acceptsRole(String name) {
        return roleDefs().stream().anyMatch((roleDef) -> roleDef.name().equals(name));
    }
}

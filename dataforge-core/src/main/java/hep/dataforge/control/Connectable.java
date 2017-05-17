/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control;

import hep.dataforge.description.DescriptorUtils;

import java.util.List;
import java.util.Optional;

/**
 * Something that could be connected
 *
 * @author Alexander Nozik
 */
public interface Connectable {

    /**
     * Register a new connection with given roles
     *
     * @param connection
     * @param roles
     */
    void connect(Connection connection, String... roles);

    /**
     * A list of all available roles
     *
     * @return
     */
    default List<RoleDef> roleDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), RoleDef.class, true);
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

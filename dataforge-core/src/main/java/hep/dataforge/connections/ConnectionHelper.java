/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hep.dataforge.connections;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Stream;

/**
 * A helper class to manage Connectible objects in the same fashion
 */
public class ConnectionHelper implements Connectible {
    //TODO isolate errors inside connections
    private final Map<Connection, Set<String>> connections = new HashMap<>();
    private final Connectible caller;
    private final Logger logger;

    public ConnectionHelper(Connectible caller, Logger logger) {
        this.caller = caller;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Attach connection
     *
     * @param connection
     * @param roles
     */
    @Override
    public synchronized void connect(Connection connection, String... roles) {
        getLogger().info("Attaching connection {} with roles {}", connection.toString(), String.join(", ", roles));
        //Checking if connection could serve given roles
        for (String role : roles) {
            if (!acceptsRole(role)) {
                getLogger().warn("The connectible does not support role {}", role);
            } else {
                roleDefs().stream().filter((roleDef) -> roleDef.name().equals(role)).forEach(rd -> {
                    if (!rd.objectType().isInstance(connection)) {
                        getLogger().error("Connection does not meet type requirement for role {}. Must be {}.",
                                role, rd.objectType().getName());
                    }
                });
            }
        }

        Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        if (this.connections.containsKey(connection)) {
            //updating roles of existing connection
            connections.get(connection).addAll(roleSet);
        } else {
            this.connections.put(connection, roleSet);
        }

        try {
            getLogger().debug("Opening connection {}", connection.toString());
            connection.open(caller);
        } catch (Exception ex) {
            throw new RuntimeException("Can not open connection", ex);
        }
    }

    /**
     * Build connection (or connections if meta has multiple "connection" entries) and connect
     *
     * @param context
     * @param meta
     */
    public void connect(Context context, Meta meta) {
        if (meta.hasMeta("connection")) {
            meta.getMetaList("connection").forEach(it -> connect(context, it));
        } else {
            String[] roles = meta.getStringArray("role", new String[]{});
            Connection.buildConnection(caller, context, meta).ifPresent(connection -> connect(connection, roles));
        }
    }

    @Override
    public synchronized void disconnect(Connection connection) {
        if (connections.containsKey(connection)) {
            String conName = Named.nameOf(connection);
            try {
                getLogger().debug("Closing connection {}", conName);
                connection.close();
            } catch (Exception ex) {
                getLogger().error("Can not close connection", ex);
            }
            getLogger().info("Detaching connection {}", conName);
            this.connections.remove(connection);
        }
    }


    /**
     * Get a stream of all connections for a given role. Stream could be empty
     *
     * @param role
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> Stream<T> connections(String role, Class<T> type) {
        return connections.entrySet().stream()
                .filter(entry -> type.isInstance(entry.getKey()))
                .filter(entry -> entry.getValue().stream().anyMatch(r -> r.matches(role)))
                .map(entry -> type.cast(entry.getKey()));
    }

    /**
     * Return a unique connection or first connection satisfying condition
     *
     * @param role
     * @param type
     * @param <T>
     * @return
     */
    public <T> Optional<T> optConnection(String role, Class<T> type) {
        return connections(role, type).findFirst();
    }

    @Override
    public boolean acceptsRole(String name) {
        return caller.acceptsRole(name);
    }

    @Override
    public List<RoleDef> roleDefs() {
        return caller.roleDefs();
    }

    @Override
    public Optional<RoleDef> optRoleDef(String name) {
        return caller.optRoleDef(name);
    }
}
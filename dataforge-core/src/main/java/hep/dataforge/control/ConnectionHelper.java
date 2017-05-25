package hep.dataforge.control;

import hep.dataforge.names.Named;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Stream;

/**
 * A helper class to manage Connectible objects in the same fashion
 */
public class ConnectionHelper implements Connectible {
    //TODO isolate errors inside connections
    private final Map<Connection<?>, Set<String>> connections = new HashMap<>();
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
    @SuppressWarnings("unchecked")
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
        if(this.connections.containsKey(connection)){
            //updating roles of existing connection
            connections.get(connection).addAll(roleSet);
        } else {
            this.connections.put(connection, roleSet);
        }
        try {
            getLogger().debug("Opening connection {}", connection.toString());
            connection.open(caller);
        } catch (Exception ex) {
            getLogger().error("Can not open connection", ex);
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


    @Override
    public <T> Stream<T> connections(String role, Class<T> type) {
        return connections.entrySet().stream()
                .filter(entry -> type.isInstance(entry.getKey()))
                .filter(entry -> entry.getValue().stream().anyMatch(r -> r.matches(role)))
                .map(entry -> type.cast(entry.getKey()));
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

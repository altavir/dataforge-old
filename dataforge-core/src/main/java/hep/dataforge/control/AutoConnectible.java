package hep.dataforge.control;

import java.util.Optional;
import java.util.stream.Stream;

public interface AutoConnectible extends Connectible {

    ConnectionHelper getConnectionHelper();

    @Override
    default void connect(Connection connection, String... roles) {
        getConnectionHelper().connect(connection, roles);
    }

    @Override
    default <T> Stream<T> connections(String role, Class<T> type) {
        return getConnectionHelper().connections(role, type);
    }

    default <T> Optional<T> connection(String role, Class<T> type) {
        return getConnectionHelper().connection(role, type);
    }

    @Override
    default void disconnect(Connection connection) {
        getConnectionHelper().disconnect(connection);
    }
}

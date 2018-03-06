package hep.dataforge.control.devices;

import hep.dataforge.connections.Connection;
import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A hub containing several devices
 */
public interface DeviceHub extends Provider {
    String DEVICE_TARGET = "device";

    Optional<Device> optDevice(Name name);

    Stream<Name> getDeviceNames();

    @Provides(DEVICE_TARGET)
    default Optional<Device> optDevice(String name) {
        return optDevice(Name.of(name));
    }

    @ProvidesNames(DEVICE_TARGET)
    default Stream<String> listDevices() {
        return getDeviceNames().map(Name::toString);
    }

    default Stream<Device> getDevices(boolean recursive) {
        if (recursive) {
            return getDeviceNames().map(it -> optDevice(it).get());
        } else {
            return getDeviceNames().filter(it -> it.getLength() == 1).map(it -> optDevice(it).get());
        }
    }

    /**
     * Add a connection to each of child devices
     *
     * @param connection
     * @param roles
     */
    default void connectAll(Connection connection, String... roles) {
        getDeviceNames().filter(it -> it.getLength() == 1)
                .map(this::optDevice)
                .map(Optional::get)
                .forEach(it -> {
                    if (it instanceof DeviceHub) {
                        ((DeviceHub) it).connectAll(connection, roles);
                    } else {
                        it.connect(connection, roles);
                    }
                });
    }

    default void connectAll(Context context, Meta meta) {
        getDeviceNames().filter(it -> it.getLength() == 1)
                .map(this::optDevice)
                .map(Optional::get)
                .forEach(it -> {
                    if (it instanceof DeviceHub) {
                        ((DeviceHub) it).connectAll(context, meta);
                    } else {
                        it.getConnectionHelper().connect(context, meta);
                    }
                });
    }
}

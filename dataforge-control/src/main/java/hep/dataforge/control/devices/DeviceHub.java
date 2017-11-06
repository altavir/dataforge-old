package hep.dataforge.control.devices;

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

    Stream<Name> deviceNames();

    @Provides(DEVICE_TARGET)
    default Optional<Device> optDevice(String name) {
        return optDevice(Name.of(name));
    }

    @ProvidesNames(DEVICE_TARGET)
    default Stream<String> listDevices() {
        return deviceNames().map(Name::toString);
    }

}

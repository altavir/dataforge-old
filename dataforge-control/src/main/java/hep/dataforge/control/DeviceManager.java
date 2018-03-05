package hep.dataforge.control;

import hep.dataforge.connections.Connection;
import hep.dataforge.context.*;
import hep.dataforge.control.devices.Device;
import hep.dataforge.control.devices.DeviceFactory;
import hep.dataforge.control.devices.DeviceHub;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A plugin for creating and using different devices
 * Created by darksnake on 11-Oct-16.
 */
@PluginDef(name = "devices", info = "Management plugin for devices an their interaction")
public class DeviceManager extends BasicPlugin implements Dispatcher, DeviceHub {

    /**
     * Registered devices
     */
    private Map<Name, Device> devices = new HashMap<>();


    public void add(Device device) {
        Name name = Name.ofSingle(device.getName());
        if (devices.containsKey(name)) {
            getLogger().warn("Replacing existing device in device manager!");
            remove(name);
        }
        devices.put(name, device);
    }

    public void remove(Name name) {
        Optional.ofNullable(this.devices.remove(name)).ifPresent(it -> {
            try {
                it.shutdown();
            } catch (ControlException e) {
                getLogger().error("Failed to stop the device: " + it.getName(), e);
            }
        });
    }


    public Device buildDevice(Meta deviceMeta) {
        DeviceFactory factory = getContext()
                .optService(DeviceFactory.class, f -> Objects.equals(f.getType(), ControlUtils.getDeviceType(deviceMeta)))
                .orElseThrow(() -> new RuntimeException("Can't find factory for given device type"));
        Device device = factory.build(getContext(), deviceMeta);

        deviceMeta.getMetaList("connection").forEach(connectionMeta -> {
            device.getConnectionHelper().connect(getContext(), connectionMeta);
        });

        add(device);
        return device;
    }

    @NotNull
    @Override
    public Responder getResponder(@NotNull Meta targetInfo) throws EnvelopeTargetNotFoundException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Responder getResponder(@NotNull Envelope envelope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Device> optDevice(Name name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Can't provide a device with zero name");
        } else if (name.getLength() == 1) {
            return Optional.ofNullable(devices.get(name));
        } else {
            return Optional.ofNullable(devices.get(name.getFirst())).flatMap(hub -> {
                if (hub instanceof DeviceHub) {
                    return ((DeviceHub) hub).optDevice(name.cutFirst());
                } else {
                    return Optional.empty();
                }
            });
        }
    }

    @Override
    public Stream<Name> getDeviceNames() {
        return devices.entrySet().stream().flatMap(entry -> {
            if (entry.getValue() instanceof DeviceHub) {
                return ((DeviceHub) entry.getValue()).getDeviceNames().map(it -> entry.getKey().append(it));
            } else {
                return Stream.of(entry.getKey());
            }
        });
    }

    /**
     * Get the stream of top level devices
     *
     * @return
     */
    public Stream<Device> getDevices() {
        return devices.values().stream();
    }

    @Override
    public void detach() {
        devices.values().forEach(it -> {
            try {
                it.shutdown();
            } catch (ControlException e) {
                getLogger().error("Failed to stop the device: " + it.getName(), e);
            }
        });
        super.detach();
    }

    @Override
    public void connectAll(Connection connection, String... roles) {
        this.devices.values().forEach(device -> device.connect(connection, roles));
    }

    @Override
    public void connectAll(Context context, Meta meta) {
        this.devices.values().forEach(device -> device.getConnectionHelper().connect(context, meta));
    }

    public static class Factory extends PluginFactory {
        @NotNull
        @Override
        public Plugin build(@NotNull Meta meta) {
            return new DeviceManager();
        }

        @NotNull
        @Override
        public Class<? extends Plugin> getType() {
            return DeviceManager.class;
        }
    }
}

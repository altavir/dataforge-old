package hep.dataforge.control;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.PluginDef;
import hep.dataforge.control.devices.Device;
import hep.dataforge.control.devices.DeviceFactory;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ReferenceRegistry;

import java.util.Objects;
import java.util.Optional;

/**
 * A plugin for creating and using different devices
 * Created by darksnake on 11-Oct-16.
 */
@PluginDef(name = "control", description = "Management plugin for devices an their interaction")
public class ControlPlugin extends BasicPlugin implements Dispatcher {

    /**
     * Registered devices
     */
    ReferenceRegistry<Device> devices = new ReferenceRegistry<>();

    @SuppressWarnings("unchecked")
    private <D extends Device> D buildDevice(Meta deviceMeta) {
        D device = getContext()
                .findService(DeviceFactory.class, f -> Objects.equals(f.getType(), ControlUtils.getDeviceType(deviceMeta)))
                .map(it -> (DeviceFactory<D>) it)
                .map(factory -> factory.build(getContext(), meta()))
                .orElseThrow(() -> new RuntimeException("Can't find factory for given device type"));
        devices.add(device);
        return device;
    }

    @Override
    public Responder getResponder(Meta targetInfo) throws EnvelopeTargetNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Optional<Device> getDeviceByName(String name) {
        return devices.findFirst(it -> getName().equals(name));
    }

}

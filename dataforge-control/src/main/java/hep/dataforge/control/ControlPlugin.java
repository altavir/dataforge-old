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
@PluginDef(name = "control", info = "Management plugin for devices an their interaction")
public class ControlPlugin extends BasicPlugin implements Dispatcher {

    /**
     * Registered devices
     */
    ReferenceRegistry<Device> devices = new ReferenceRegistry<>();

    @SuppressWarnings("unchecked")
    private <D extends Device> D buildDevice(Meta deviceMeta) {
        DeviceFactory<D> factory = getContext()
                .findService(DeviceFactory.class, f -> Objects.equals(f.getType(), ControlUtils.getDeviceType(deviceMeta)))
                .orElseThrow(()->new RuntimeException("Can't find factory for given device type"));
        D device = factory.build(getContext(),deviceMeta);

        deviceMeta.getMetaList("connect").forEach(connectionMeta ->{

        });

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

package hep.dataforge.control;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.PluginDef;
import hep.dataforge.control.devices.Device;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaFactory;
import hep.dataforge.utils.ReferenceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A plugin for creating and using different devices
 * Created by darksnake on 11-Oct-16.
 */
@PluginDef(name = "control", description = "Management plugin for devices an their interaction")
public class ControlPlugin extends BasicPlugin implements Dispatcher {

    /**
     * registered device factories
     */
    private Map<String, MetaFactory<Device>> deviceFactoryMap = new HashMap<>();

    /**
     * Registered devices
     */
    ReferenceRegistry<Device> devices;

    public Device buildDevice(Meta deviceMeta) {
        MetaFactory<Device> factory = getDeviceFactory(deviceMeta);
        if(factory!= null){
            Device device = factory.build(meta());
//            String deviceName = ControlUtils.getDeviceName(deviceMeta);
            device.setContext(getContext());
            devices.add(device);
            return device;
        } else {
            throw new RuntimeException("Can't find factory for given device type");
        }
    }

    private MetaFactory<Device> getDeviceFactory(Meta deviceMeta){
        String deviceType = ControlUtils.getDeviceType(deviceMeta);
        return deviceFactoryMap.get(deviceType);
    }

    @Override
    public Responder getResponder(Meta targetInfo) throws EnvelopeTargetNotFoundException {
        return null;
    }
}

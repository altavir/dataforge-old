/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.devices.Device;
import hep.dataforge.control.devices.DeviceListener;

/**
 * A controller for device MVC visualization pattern
 *
 * @author Alexander Nozik
 */
public abstract class DeviceViewController extends DeviceConnection implements DeviceListener, MeasurementConsumer {

    @Override
    public void open(Device device) throws Exception {
        super.open(device);
        device.addDeviceListener(this);
    }

    @Override
    public void close() throws Exception {
        if (isOpen()) {
            getDevice().removeDeviceListener(this);
        }
        super.close();
    }

}

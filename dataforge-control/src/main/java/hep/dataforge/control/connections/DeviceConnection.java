/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.Connection;
import hep.dataforge.control.devices.Device;

public abstract class DeviceConnection implements Connection {

    private Device device;

    public Device getDevice() {
        if (device != null) {
            return device;
        } else {
            throw new RuntimeException("Not connected!");
        }
    }

    @Override
    public boolean isOpen() {
        return this.device != null;
    }

    @Override
    public void open(Object device) throws Exception {
        this.device = Device.class.cast(device);
    }

    @Override
    public void close() throws Exception {
        this.device = null;
    }

}

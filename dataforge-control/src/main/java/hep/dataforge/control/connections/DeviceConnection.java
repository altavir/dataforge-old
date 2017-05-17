/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.Connection;
import hep.dataforge.control.devices.Device;

public abstract class DeviceConnection<D extends Device> implements Connection<D> {

    private D device;

    public D getDevice() {
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
    public void open(D device) throws Exception {
        this.device = device;
    }

    @Override
    public void close() throws Exception {
        this.device = null;
    }

}

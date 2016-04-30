/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.events.EventBuilder;

/**
 *
 * @author Alexander Nozik
 */
public class DeviceEventBuilder extends EventBuilder<DeviceEventBuilder> {

    public static final String DEVICE_EVENT_TYPE = "device";

    /**
     * Construct event builder with 
     * @param subtype 
     */
    public DeviceEventBuilder(String subtype) {
        super(DEVICE_EVENT_TYPE + "." + subtype);
    }
    
    /**
     * Should be called before any supertype builder methods.
     * @param device
     * @return 
     */
    public DeviceEventBuilder setDevice(Device device){
        setSource(device.getName());
        super.addReference("device", device);
        return this;
    }

    @Override
    public DeviceEventBuilder self() {
        return this;
    }
    
    

}

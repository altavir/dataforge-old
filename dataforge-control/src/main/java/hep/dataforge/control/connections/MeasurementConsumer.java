/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.devices.Device;
import hep.dataforge.control.measurements.Measurement;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public interface MeasurementConsumer {
    void accept(Device device, String measurementName, Measurement measurement);
    
    default void accept(Device device, Measurement measurement){
        accept(device, null, measurement);
    }
}

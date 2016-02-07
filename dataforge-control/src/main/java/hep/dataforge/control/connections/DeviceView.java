/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.devices.DeviceListener;
import javafx.scene.Node;

/**
 * Base for FX device visual component
 *
 * @author Alexander Nozik
 */
public abstract class DeviceView extends DeviceConnection implements DeviceListener, MeasurementListenerFactory {

    public abstract Node getComponent();

    @Override
    public String type() {
        return "view";
    }
}

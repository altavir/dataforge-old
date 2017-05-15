/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.control.devices.annotations.StateDef;
import hep.dataforge.control.measurements.Sensor;
import hep.dataforge.control.ports.PortFactory;
import hep.dataforge.control.ports.PortHandler;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ControlException;

import static hep.dataforge.control.devices.PortSensor.CONNECTION_STATE;

/**
 * A Sensor that uses a PortHandler to obtain data
 *
 * @author darksnake
 * @param <T>
 */
@StateDef(name = CONNECTION_STATE, info = "The connection state for this device")
//@StateDef(name = PORT_NAME_STATE, info = "The name of the port this device connected to")
@ValueDef(name = "port",info = "The name of the port for this sensor")
@ValueDef(name = "timeout", type = "NUMBER", def = "400", info = "A timeout for port response")
public abstract class PortSensor<T> extends Sensor<T> {

    public static final String CONNECTION_STATE = "connected";
    public static final String PORT_NAME_KEY = "port";

    private PortHandler handler;
//    private final String portName;

//    public PortSensor(String portName) {
//        this.portName = portName;
//    }

//    public PortSensor(PortHandler handler) {
//        this.handler = handler;
//        portName = handler.getPortId();
//    }

    protected final void setHandler(PortHandler handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return getState(CONNECTION_STATE).booleanValue();
    }

    protected int timeout() {
        return meta().getInt("timeout", 400);
    }

    protected PortHandler buildHandler(String portName) throws ControlException {
        getLogger().info("Connecting to port {}", portName);
        return PortFactory.getdPort(portName);
    }

    @Override
    public void shutdown() throws ControlException {
        super.shutdown();
        try {
            if (handler != null) {
                handler.close();
            }
            updateState(CONNECTION_STATE, false);
        } catch (Exception ex) {
            throw new ControlException(ex);
        }
    }

    /**
     * @return the handler
     * @throws hep.dataforge.exceptions.ControlException
     */
    protected PortHandler getHandler() throws ControlException {
        if (handler == null) {
            String port = meta().getString(PORT_NAME_KEY);
            this.handler = buildHandler(port);
            updateState(PORT_NAME_KEY, port);
        }

        if (!handler.isOpen()) {
            updateState(CONNECTION_STATE, false);
            handler.open();
            updateState(CONNECTION_STATE, true);
        }

        return handler;
    }

}

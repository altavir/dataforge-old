/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.control.ports.PortFactory;
import hep.dataforge.control.ports.PortHandler;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.values.Value;

import java.util.Objects;

import static hep.dataforge.control.devices.PortSensor.CONNECTED_STATE;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * A Sensor that uses a PortHandler to obtain data
 *
 * @param <T>
 * @author darksnake
 */
@StateDef(
        value = @ValueDef(name = CONNECTED_STATE, def = "false", info = "The connection state for this device"),
        writable = true
)
@ValueDef(name = "port", info = "The name of the port for this sensor")
@ValueDef(name = "timeout", type = {NUMBER}, def = "400", info = "A timeout for port response")
public abstract class PortSensor<T> extends Sensor<T> {

    public static final String CONNECTED_STATE = "connected";
    public static final String PORT_NAME_KEY = "port";

    private PortHandler handler;

    protected final void setHandler(PortHandler handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return getState(CONNECTED_STATE).booleanValue();
    }

    protected int timeout() {
        return meta().getInt("timeout", 400);
    }

    protected PortHandler buildHandler(String portName) throws ControlException {
        getLogger().info("Connecting to port {}", portName);
        return PortFactory.getPort(portName);
    }

    @Override
    protected Object computeState(String stateName) throws ControlException {
        if (CONNECTED_STATE.equals(stateName)) {
            return handler != null && handler.isOpen();
        } else {
            return super.computeState(stateName);
        }
    }

    @Override
    public void shutdown() throws ControlException {
        super.shutdown();
        try {
            if (handler != null) {
                handler.close();
            }
            updateState(CONNECTED_STATE, false);
        } catch (Exception ex) {
            throw new ControlException(ex);
        }
    }

    @Override
    protected void requestStateChange(String stateName, Value value) throws ControlException {
        if (Objects.equals(stateName, CONNECTED_STATE)) {
            if (value.booleanValue()) {
                if (!getHandler().isOpen()) {
                    getHandler().open();
                }
            } else {
                try {
                    getHandler().close();
                } catch (Exception e) {
                    throw new ControlException(e);
                }
            }
        }
        super.requestStateChange(stateName, value);
    }

    /**
     * @return the handler
     * @throws hep.dataforge.exceptions.ControlException
     */
    protected PortHandler getHandler() throws ControlException {
        if (handler == null) {
            String port = meta().getString(PORT_NAME_KEY);
            this.handler = buildHandler(port);
            handler.open();
            updateState(CONNECTED_STATE, true);
        }
        return handler;
    }

}

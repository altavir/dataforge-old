/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices;

import hep.dataforge.context.Context;
import hep.dataforge.control.ports.GenericPortController;
import hep.dataforge.control.ports.Port;
import hep.dataforge.control.ports.PortFactory;
import hep.dataforge.description.ValueDef;
import hep.dataforge.events.EventBuilder;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

import static hep.dataforge.control.devices.PortSensor.CONNECTED_STATE;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * A Sensor that uses a Port to obtain data
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

    public PortSensor(Context context, Meta meta) {
        super(context, meta);
    }

    //private Port port;
    private GenericPortController connection;

//    protected final void setPort(Port port) {
//        this.connection = new GenericPortController(port);
//    }

    public boolean isConnected() {
        return getState(CONNECTED_STATE).booleanValue();
    }

    protected Duration getTimeout() {
        return Duration.ofMillis(getMeta().getInt("timeout", 400));
    }

    protected Port buildPort(String portName) throws ControlException {
        getLogger().info("Connecting to port {}", portName);
        return PortFactory.getPort(portName);
    }

    @Override
    protected Object computeState(String stateName) throws ControlException {
        if (CONNECTED_STATE.equals(stateName)) {
            return connection != null && connection.getPort().isOpen();
        } else {
            return super.computeState(stateName);
        }
    }

    @Override
    public void init() throws ControlException {
        super.init();
        if (connection == null) {
            String port = getMeta().getString(PORT_NAME_KEY);
            this.connection = new GenericPortController(getContext(), buildPort(port));
            //Add debug listener
            if (getMeta().getBoolean("debugMode", false)) {
                connection.weakOnPhrase(phrase -> getLogger().debug("Device {} received phrase: {}", getName(), phrase));
                connection.weakOnError((message, error) -> getLogger().error("Device {} exception: {}", getName(), message, error));
            }

            connection.open();
            updateState(CONNECTED_STATE, true);
        }
    }

    @Override
    public void shutdown() throws ControlException {
        try {
            connection.close();
            //PENDING do we need not to close the port sometimes. Should it be configurable?
            connection.getPort().close();
            updateState(CONNECTED_STATE, false);
        } catch (Exception ex) {
            throw new ControlException(ex);
        }
        super.shutdown();
    }

    protected final String sendAndWait(String request) {
        return connection.sendAndWait(request, getTimeout(), it -> true);
    }

    protected final String sendAndWait(String request, Predicate<String> predicate) {
        return connection.sendAndWait(request, getTimeout(), predicate);
    }

    protected final void send(String message) {
        connection.send(message);
        dispatchEvent(
                EventBuilder
                        .make(getName())
                        .setMetaValue("request", message)
                        .build()
        );
    }

    protected GenericPortController getConnection() {
        if (connection == null) {
            throw new RuntimeException("Requesting connection on not initialized device");
        }
        return connection;
    }

    @Override
    protected void requestStateChange(String stateName, Value value) throws ControlException {
        if (Objects.equals(stateName, CONNECTED_STATE)) {
            if (value.booleanValue()) {
                connection.open();
                updateState(CONNECTED_STATE, true);
            } else {
                try {
                    connection.close();
                    updateState(CONNECTED_STATE, false);
                } catch (Exception e) {
                    throw new ControlException("Failed to close the connection", e);
                }
            }
        }
    }

    /*
     * @return the port
     * @throws hep.dataforge.exceptions.ControlException
     */
//    protected Port getPort() throws ControlException {
//        if (port == null) {
//            String port = meta().getString(PORT_NAME_KEY);
//            setPort(buildPort(port));
//            this.port.open();
//            updateState(CONNECTED_STATE, true);
//        }
//        return port;
//    }

}

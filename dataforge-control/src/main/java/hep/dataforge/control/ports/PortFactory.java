/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.ports;

import hep.dataforge.exceptions.ControlException;
import jssc.SerialPortException;

/**
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class PortFactory {

    public static PortHandler buildPort(String portName) throws ControlException {
        String protocol;
        String addres;
        int port;
        if (portName.contains("::")) {
            String[] split = portName.split("::");
            protocol = split[0];
            addres = split[1];
        } else if (portName.contains(".")) {
            protocol = "tcp";
            addres = portName;
        } else {
            protocol = "com";
            addres = portName;
        }

        switch (protocol) {
            case "com":
                try {
                    return new ComPortHandler(addres);
                } catch (SerialPortException ex) {
                    throw new ControlException("Can't bind com port",ex);
                }
            case "tcp":
                if (addres.contains(":")) {
                    String[] split = addres.split(":");
                    addres = split[0];
                    port = Integer.parseInt(split[1]);
                } else {
                    port = 8080;
                }
                return new TcpPortHandler(addres, port, portName);
            default:
                throw new ControlException("Unknown protocol");
        }
    }
}

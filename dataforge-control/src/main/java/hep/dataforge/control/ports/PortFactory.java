/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.ports;

import hep.dataforge.exceptions.ControlException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class PortFactory {
    //PENDING convert to singleton?

    private static Map<String, PortHandler> portMap = new HashMap<>();

    /**
     * Create new port or reuse existing one if it is already created
     * @param portName
     * @return
     * @throws ControlException 
     */
    public synchronized static PortHandler getPort(String portName) throws ControlException {
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
        
        String canonPortName = protocol + ":"+ addres;

        if (portMap.containsKey(canonPortName)) {
            return portMap.get(canonPortName);
        } else {

            PortHandler res;
            switch (protocol) {
                case "com":
                    res = new ComPortHandler(addres);
                    break;
                case "tcp":
                    if (addres.contains(":")) {
                        String[] split = addres.split(":");
                        addres = split[0];
                        port = Integer.parseInt(split[1]);
                    } else {
                        port = 8080;
                    }
                    res = new TcpPortHandler(addres, port);
                    break;
                default:
                    throw new ControlException("Unknown protocol");
            }
            portMap.put(canonPortName, res);
            return res;
        }
    }
    
    /**
     * Register custom portHandler. Useful for virtual ports
     * @param handler 
     */
    public static void registerPort(PortHandler handler){
        if(portMap.containsKey(handler.getPortId())){
            throw new RuntimeException("Port with given id already exists");
        } else {
            portMap.put(handler.getPortId(), handler);
        }
    }
}

/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.control.ports;

import hep.dataforge.exceptions.PortException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static jssc.SerialPort.*;

/**
 * @author Alexander Nozik
 */
public class ComPort extends Port implements SerialPortEventListener {

    //    private static final int CHAR_SIZE = 1;
//    private static final int MAX_SIZE = 50;
    private SerialPort port;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public ComPort(Meta meta) {
        super(meta);
    }

    @Override
    public String getPortId() {
        return String.format("com::%s", getString("name"));
    }


    public ComPort(String portName, int baudRate, int dataBits, int stopBits, int parity) {
        this(new MetaBuilder("handler")
                .setValue("type", "com")
                .putValue("name", portName)
                .putValue("baudRate", baudRate)
                .putValue("dataBits", dataBits)
                .putValue("stopBits", stopBits)
                .putValue("parity", parity)
                .build()
        );
    }

    /**
     * Construct ComPort with default parameters:
     * <p>
     * Baud rate: 9600 </p>
     * <p>
     * Data bits: 8 </p>
     * <p>
     * Stop bits: 1</p>
     * <p>
     * Parity: non</p>
     *
     * @param portName
     */
    public ComPort(String portName) {
        this(portName, BAUDRATE_9600, DATABITS_8, STOPBITS_1, PARITY_NONE);
    }

    @Override
    public void open() throws PortException {
        try {
            if (port == null) {
                port = new SerialPort(getString("name"));
                port.openPort();
                Meta an = meta();
                int baudRate = an.getInt("baudRate", BAUDRATE_9600);
                int dataBits = an.getInt("dataBits", DATABITS_8);
                int stopBits = an.getInt("stopBits", STOPBITS_1);
                int parity = an.getInt("parity", PARITY_NONE);
                port.setParams(baudRate, dataBits, stopBits, parity);
                port.addEventListener(this);
            }
        } catch (SerialPortException ex) {
            throw new PortException("Can't open the port", ex);
        }
    }

    public void clearPort() throws PortException {
        try {
            port.purgePort(PURGE_RXCLEAR | PURGE_TXCLEAR);
        } catch (SerialPortException ex) {
            throw new PortException(ex);
        }
    }

    @Override
    public void close() throws Exception {
        if (port != null) {
            if (port.isOpened()) {
                port.removeEventListener();
                port.closePort();
            }
            port = null;
        }
    }

    @Override
    public void send(String message) throws PortException {
        if (!isOpen()) {
            open();
        }
        try {
            LoggerFactory.getLogger(getClass()).debug("SEND: " + message);
            port.writeString(message);
        } catch (SerialPortException ex) {
            throw new PortException(ex);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {

            int chars = serialPortEvent.getEventValue();
            byte[] bytes = new byte[chars];
            try {
                bytes = port.readBytes(chars);
            } catch (SerialPortException ex) {
                controller.portError("Internal JSSC error", ex);
            }
            try {
                buffer.write(bytes);
            } catch (IOException ex) {
                throw new Error(ex);
            }

            String str = new String(buffer.toByteArray());
            if (isPhrase(str)) {
                receivePhrase(str);
                buffer.reset();
            }

        }
    }

    @Override
    public boolean isOpen() {
        return port != null;
    }
}

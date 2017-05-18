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
import hep.dataforge.meta.MetaBuilder;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Alexander Nozik
 */
public class TcpPortHandler extends PortHandler {

    private Socket socket;

    private Thread listenerThread;

    private volatile boolean stopFlag = false;

    public TcpPortHandler(String ip, int port) throws PortException {
        super(new MetaBuilder("handler")
                .setValue("type", "tcp")
                .setValue("ip", ip)
                .setValue("port", port)
                .build());
    }

    @Override
    public String getPortId() {
        return String.format("tcp::%s:%s", getString("ip"), getString("port"));
    }

    @Override
    public void open() throws PortException {
        try {
            if (listenerThread == null) {
                stopFlag = false;
                listenerThread = startListenerThread();
            }
        } catch (IOException ex) {
            throw new PortException(ex);
        }
    }

    //    @Override
//    public void holdBy(PortController controller) throws PortException {
//        super.holdBy(controller); //To change body of generated methods, choose Tools | Templates.
//
//        open();
//
//    }
    @Override
    public synchronized void close() throws PortException {
        if (socket != null) {
            try {
                stopFlag = true;
                if (listenerThread != null) {
                    listenerThread.join(1500);
                }
            } catch (InterruptedException ex) {
                throw new PortException(ex);
            } finally {
                listenerThread = null;
                try {
                    getSocket().close();
                    socket = null;
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Failed to close socket", e);
                }
            }
        }
    }

    private Thread startListenerThread() throws IOException {
        Runnable task = () -> {
            BufferedInputStream reader = null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while (!stopFlag) {
                try {
                    if (reader == null) {
                        reader = new BufferedInputStream(getSocket().getInputStream());
                    }
                    buffer.write(reader.read());
                    String str = buffer.toString();
                    if (isPhrase(str)) {
                        recievePhrase(str);
                        buffer.reset();
                    }
                } catch (IOException ex) {
                    if (!stopFlag) {
                        LoggerFactory.getLogger(getClass()).error("TCP connection broken on {}. Reconnecting.", getPortId());
                        try {
                            if (socket != null) {
                                socket.close();
                                socket = null;
                            }
                            reader = new BufferedInputStream(getSocket().getInputStream());
                        } catch (Exception ex1) {
                            throw new RuntimeException("Failed to reconnect tcp port");
                        }
                    }
                }
            }

        };
        Thread thread = new Thread(task, "tcpPortListener");
        thread.start();

        return thread;
    }

    @Override
    public void send(String message) throws PortException {
        try {
            OutputStream stream = getSocket().getOutputStream();
            stream.write(message.getBytes());
            stream.flush();
            LoggerFactory.getLogger(getClass()).debug("SEND: " + message);
        } catch (IOException ex) {
            throw new PortException(ex);
        }

    }

    @Override
    public boolean isOpen() {
        return listenerThread != null;
    }

    public synchronized Socket getSocket() throws IOException {
        if (socket == null) {
            socket = new Socket(getString("ip"), getInt("port"));
        }
        return socket;
    }
}

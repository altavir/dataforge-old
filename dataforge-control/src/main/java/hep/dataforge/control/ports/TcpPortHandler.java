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

    private final Socket socket;

    private Thread listenerThread;

    private volatile boolean stopFlag = false;

    //TODO сделать аннотацию и конструктор по аннотации
    public TcpPortHandler(String ip, int port) throws PortException {
        try {
            socket = new Socket(ip, port);
        } catch (IOException ex) {
            throw new PortException(ex);
        }

    }

    public TcpPortHandler(Socket socket) throws IOException {
        this.socket = socket;
    }

    @Override
    public String getPortId() {
        return String.format("tcp::%s:%s", socket.getInetAddress(), socket.getPort());
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
    public void close() throws PortException {
        try {
            stopFlag = true;
            listenerThread.join(1500);
        } catch (InterruptedException ex) {
            throw new PortException(ex);
        } finally {
            listenerThread = null;
            try {
                socket.close();
            } catch (IOException e) {
                throw new PortException(e);
            }
        }
    }

    private Thread startListenerThread() throws IOException {
        final BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Runnable task = () -> {
            try {
                while (!stopFlag) {
                    buffer.write(reader.read());
                    String str = buffer.toString();
                    if (isPhrase(str)) {
                        recievePhrase(str);
                        buffer.reset();
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        Thread thread = new Thread(task, "tcpPortListener");
        thread.start();

        return thread;
    }

    @Override
    public void send(String message) throws PortException {
        try {
            OutputStream stream = socket.getOutputStream();
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

    @Override
    public Meta meta() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

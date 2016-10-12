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
package hep.dataforge.storage.commons;

import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static hep.dataforge.io.envelopes.Envelope.DATA_TYPE_KEY;

/**
 * Abstract network listener for envelopes
 *
 * @author Darksnake
 */
public abstract class AbstractNetworkListener implements Annotated, AutoCloseable, Responder {

    private static final Logger logger = LoggerFactory.getLogger("LISTENER");

    private volatile boolean finishflag = false;
    private ServerSocket serverSocket;
    private final Meta config;

    public AbstractNetworkListener(Meta listnerConfig) {
        if (listnerConfig == null) {
            this.config = Meta.buildEmpty("listener");
        } else {
            this.config = listnerConfig;
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        finishflag = true;
        if (serverSocket != null) {
            serverSocket.close();
        }
        logger.info("Closing listner...");
    }

    @Override
    public Meta meta() {
        return config;
    }

    private int getPort() {
        return meta().getInt("port", 8335);
    }

    @Override
    public abstract Envelope respond(Envelope message);

    public void open() throws Exception {
        ThreadGroup clientGroup = new ThreadGroup("clients");
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(getPort())) {
                serverSocket = ss;
                logger.info("Starting to listning to the port {}", getPort());
                while (!finishflag) {
                    //FIXME add timeout
                    Socket s = ss.accept();
                    logger.info("Client accepted from {}", s.getRemoteSocketAddress().toString());
//                    new SocketProcessor(s).run();
                    SocketProcessor socketProcessor = new SocketProcessor(s);
                    new Thread(clientGroup, socketProcessor).start();
                }
            } catch (IOException ex) {
                if (!finishflag) {
                    logger.error("Connection exception", ex);
                }
            }
            logger.info("Listener closed");
            serverSocket = null;
        }, "listner").start();
    }

    /**
     * Decide to accept envelope
     *
     * @param envelope
     * @return
     */
    protected boolean accept(Envelope envelope) {
        return true;
    }

    protected Envelope terminator() {
        return new EnvelopeBuilder()
                .setEnvelopeType(EnvelopeCodes.DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setDataType(EnvelopeCodes.MESSAGE_TERMINATOR_CODE)
                .build();
    }

    protected MessageFactory getResponseFactory() {
        return new MessageFactory();
    }

    private class SocketProcessor implements Runnable {

        private final Socket socket;
        private final InputStream is;
        private final OutputStream os;

        private SocketProcessor(Socket s) throws IOException {
            this.socket = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
        }

        @Override
        public void run() {
            logger.info("Starting client processing from {}", socket.getRemoteSocketAddress().toString());
            while (!finishflag) {
                if (socket.isClosed()) {
                    finishflag = true;
                    logger.debug("Socket {} closed by client", socket.getRemoteSocketAddress().toString());
                    break;
                }
                try {
                    Envelope request = read();
                    //Breaking connection on terminator
                    if (isTerminator(request)) {
                        logger.info("Recieved terminator message from {}", socket.getRemoteSocketAddress().toString());
                        break;
                    }
                    if (accept(request)) {
                        Envelope response;
                        try {
                            response = respond(request);
                        } catch (Exception ex) {
                            logger.error("Uncatched exception during response evaluation", ex);
                            response = getResponseFactory().errorResponseBase("", ex).build();
                        }
                        //Null respnses are ignored
                        if (response != null) {
                            write(response);
                        }
                    }
                } catch (IOException ex) {
                    logger.error("IO exception during envelope evaluation", ex);
                    finishflag = true;
                }
            }

            logger.info("Client processing finished for {}", socket.getRemoteSocketAddress().toString());
            if (!socket.isClosed()) {
                try {
                    write(terminator());//Sending additiona terminator to notify client that server is closing connection
                } catch (IOException ex) {
                    logger.error("Terminator send failed",ex);
                }
                try {
                    socket.close();
                } catch (IOException ex) {
                    logger.error("Can't close the socket", ex);
                }
            }
        }

        private Envelope read() throws IOException {
            return new DefaultEnvelopeReader().readWithData(is);
        }

        private void write(Envelope envelope) throws IOException {
            new DefaultEnvelopeWriter().write(os, envelope);
            os.flush();
        }

        private boolean isTerminator(Envelope envelope) {
            return envelope.getProperties().get(DATA_TYPE_KEY).intValue() == EnvelopeCodes.MESSAGE_TERMINATOR_CODE;
        }

    }
}

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
import hep.dataforge.exceptions.PortLockException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.utils.MetaHolder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * The universal asynchronous port handler
 *
 * @author Alexander Nozik
 */
public abstract class Port extends MetaHolder implements AutoCloseable, Metoid {
    /**
     * The definition of default phrase condition
     *
     * @return
     */
    private static Predicate<String> DEFAULT_PHRASE_CONDITION = (String str) -> str.endsWith("\n");

    private final ReentrantLock portLock = new ReentrantLock(true);

    private PortController controller;

    /**
     * The default end phrase condition
     */
    private Predicate<String> phraseCondition = DEFAULT_PHRASE_CONDITION;

    private ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread res = new Thread(r);
        res.setName("port::" + Port.this.toString());
        res.setPriority(Thread.MAX_PRIORITY);
        return res;
    });

    protected Port(Meta meta) {
        super(meta);
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(meta().getString("logger", getClass().getName()));
    }

    public void setPhraseCondition(Predicate<String> condition) {
        this.phraseCondition = condition;
    }

    public void setDelimiter(String delimiter) {
        phraseCondition = (String str) -> str.endsWith(delimiter);
    }

    public abstract void open() throws PortException;

    public abstract boolean isOpen();

    protected void run(Runnable r) {
        executor.submit(r);
    }

    /**
     * Emergency hold break.
     */
    public synchronized void breakHold() {
//        controller = null;
        run(portLock::unlock);
    }

    /**
     * An unique ID for this port
     *
     * @return
     */
    public abstract String toString();

    /**
     * Acquire lock on this instance of port handler with given controller
     * object. Only the same controller can release the hold.
     *
     * @param controller
     * @throws hep.dataforge.exceptions.PortException
     */
    public void holdBy(PortController controller) throws PortException {
        assert controller != null;
        if (!isOpen()) {
            open();
        }

        run(() -> {
            try {
                portLock.lockInterruptibly();
            } catch (InterruptedException ex) {
                getLogger().error("Lock on port {} is broken", toString());
                throw new RuntimeException(ex);
            }
        });
        getLogger().debug("Locked by {}", controller);
        this.controller = controller;
    }

    /**
     * The condition that should be satisfied to complete the incoming message
     *
     * @param str
     * @return
     */
    protected boolean isPhrase(String str) {
        return phraseCondition.test(str);
    }

    /**
     * This method accepts complete phrase and sends it to current controller
     *
     * @param phrase
     */
    protected synchronized void receivePhrase(String phrase) {
//        lastResponse.lazySet(phrase);
        notify();
        getLogger().trace("RECEIVE: " + phrase);
        if (controller != null) {
            controller.acceptPhrase(phrase);
        }
    }

    /**
     * send the message to the port
     *
     * @param message
     * @throws hep.dataforge.exceptions.PortException
     */
    protected abstract void send(String message) throws PortException;

    /**
     * Send the message if the controller is correct
     *
     * @param controller
     * @param message
     * @throws PortException
     */
    public final void send(@Nullable PortController controller, String message) throws PortException {
        if (this.controller == null || controller == this.controller) {
            send(message);
        } else {
            throw new PortException("Port locked by another controller");
        }
    }

    /**
     * Release hold of this portHandler from given controller.
     *
     * @param controller
     * @throws PortLockException in case given holder is not the one that holds
     *                           handler
     */
    public synchronized void releaseBy(PortController controller) throws PortLockException {
        if (isLocked()) {
            assert controller != null;
            if (controller.equals(this.controller)) {
                this.controller = null;
                run(() -> {
                    portLock.unlock();
                    getLogger().debug("Unlocked by {}", controller);
                });
            } else {
                throw new PortLockException("Can't unlock hold with wrong holder");
            }
        } else {
            getLogger().warn("Attempting to release unlocked port");
        }
    }

    public boolean isLocked() {
        return this.controller != null;
    }


    @Override
    public void close() throws Exception {
        executor.shutdown();
    }

    /**
     * The controller which is currently working with this handler. One
     * controller can simultaneously hold many handlers, but handler could be
     * held by only one controller.
     */
    public interface PortController {

        void acceptPhrase(String message);

        default void portError(String errorMessage, Throwable error) {
            //do nothing
        }
    }
}

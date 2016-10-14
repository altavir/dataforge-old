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
import hep.dataforge.meta.Annotated;
import hep.dataforge.utils.DateTimeUtils;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * The handler
 *
 * @author Alexander Nozik
 */
public abstract class PortHandler implements AutoCloseable, Annotated {

    private final ReentrantLock portLock = new ReentrantLock(true);
    protected PortController controller;

    //PENDING add additional port listeners?
    private volatile String lastResponse = null;
    /**
     * The default end phrase condition
     */
    private Predicate<String> phraseCondition = defaultPhraseCondition();
//    private final String portName;

//    public PortHandler(String portName) {
//        this.portName = portName;
//    }

    /**
     * The definition of default phrase condition
     *
     * @return
     */
    private static Predicate<String> defaultPhraseCondition() {
        return (String str) -> str.endsWith("\n");
    }

    public void setPhraseCondition(Predicate<String> condition) {
        this.phraseCondition = condition;
    }

    public void setDelimeter(String delimeter) {
        phraseCondition = (String str) -> str.endsWith(delimeter);
    }

    public abstract void open() throws PortException;

    public abstract boolean isOpen();

    /**
     * Emergency hold break.
     */
    public synchronized void breakHold() {
        controller = null;
        portLock.unlock();
    }

    /**
     * An unique ID for this port
     * @return
     */
    public abstract String getPortId();

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

        try {
            portLock.lockInterruptibly();
        } catch (InterruptedException ex) {
            LoggerFactory.getLogger(getClass()).error("Lock on port {} is broken", getPortId());
            throw new PortException(ex);
        }
        LoggerFactory.getLogger(getClass()).debug("Locked by {}", controller);
        this.controller = controller;
    }

    /**
     * The condition that should be satisfied to complete the incoming message
     *
     * @param str
     * @return
     */
    public boolean isPhrase(String str) {
        return phraseCondition.test(str);
    }

    /**
     * This method accepts complete phrase and sends it to current controller
     *
     * @param phrase
     */
    protected synchronized void recievePhrase(String phrase) {
        lastResponse = phrase;
        notify();
        LoggerFactory.getLogger(getClass()).debug("RECIEVE: " + phrase);
        if (controller != null) {
            controller.accept(phrase);
        }
    }

    /**
     * send the message to the port
     *
     * @param message
     * @throws hep.dataforge.exceptions.PortException
     */
    public abstract void send(String message) throws PortException;

    /**
     * Send the string and wait for a specific answer. All other answers are
     * passed to the controller but only this one is returned. This method
     * ignores holder lock.
     *
     * @param message
     * @param responseCondition
     * @param timeout
     * @return
     * @throws hep.dataforge.exceptions.PortException
     */
    public final synchronized String sendAndWait(String message, Predicate<String> responseCondition, int timeout)
            throws PortException {
        if (!isOpen()) {
            open();
        }
        
        send(message);
        waitForPhrase(responseCondition, Duration.ofMillis(timeout));
        return lastResponse;
    }

    /**
     * Send message and wait for the fist reply. This method ignores holder
     * lock.
     *
     * @param message
     * @param timeout
     * @return
     * @throws PortException
     */
    public final synchronized String sendAndWait(String message, int timeout) throws PortException {
        return sendAndWait(message, null, timeout);
    }

    /**
     * Waits for phrase from port which satisfies specific condition
     *
     * @param responseCondition the condition to be specified. If null than next
     * phrase is accepted
     * @throws InterruptedException
     */
    private synchronized void waitForPhrase(Predicate<String> responseCondition, Duration timeout) throws PortException {
        lastResponse = null;
        Instant start = DateTimeUtils.now();
        try {
            while (lastResponse == null
                    || (responseCondition != null && !responseCondition.test(lastResponse))) {
                if (Duration.between(start, DateTimeUtils.now()).compareTo(timeout) > 0) {
                    throw new PortTimeoutException(timeout);
                }
                wait(timeout.toMillis());
            }
        } catch (InterruptedException ex) {
            throw new PortException(ex);
        }

    }

    /**
     * Release hold of this portHandler from given controller.
     *
     * @param controller
     * @throws PortLockException in case given holder is not the one that holds
     * handler
     */
    public synchronized void unholdBy(PortController controller) throws PortLockException {
        if (isLocked()) {
            assert controller != null;
            if (controller.equals(this.controller)) {
                this.controller = null;
                portLock.unlock();
                LoggerFactory.getLogger(getClass()).debug("Unlocked by {}", controller);
            } else {
                throw new PortLockException("Can't unlock hold with wrong holder");
            }
        } else {
            LoggerFactory.getLogger(getClass()).warn("Attempting to unhold unlocked port");
        }
    }

    public boolean isLocked() {
        return this.controller != null;
    }

    /**
     * The controller which is currently working with this handler. One
     * controller can simultaneously hold many handlers, but handler could be
     * held by only one controller.
     */
    public static interface PortController {

        void accept(String message);

        void error(String errorMessage, Throwable error);
    }
}

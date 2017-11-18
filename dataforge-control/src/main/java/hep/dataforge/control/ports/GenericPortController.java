package hep.dataforge.control.ports;

import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.PortException;
import hep.dataforge.utils.ReferenceRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A port controller helper that allows both synchronous and asynchronous operations on port
 */
public class GenericPortController implements Port.PortController, AutoCloseable {

    /**
     * Use temporary controller to safely send request and receive response
     *
     * @param port
     * @param request
     * @param timeout
     * @return
     * @throws ControlException
     */
    public static String sendAndWait(Port port, String request, Duration timeout) throws ControlException {
        try (GenericPortController controller = new GenericPortController(port)) {
            return controller.sendAndWait(request, timeout, it -> true);
        }
    }

    private final Port port;

    private final ReferenceRegistry<FuturePhrase> waiters = new ReferenceRegistry<>();
    private final ReferenceRegistry<PhraseListener> listeners = new ReferenceRegistry<>();
    private final ReferenceRegistry<BiConsumer<String, Throwable>> exceptionListeners = new ReferenceRegistry<>();

    public GenericPortController(@NotNull Port port) {
        this.port = port;
        try {
            port.holdBy(this);
        } catch (PortException e) {
            throw new RuntimeException("Can't hold the port " + port + " by generic handler", e);
        }
    }

    @Override
    public void acceptPhrase(String message) {
        waiters.forEach(phrase -> phrase.acceptPhrase(message));
        listeners.forEach(phrase -> {
            try {
                phrase.acceptPhrase(message);
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).error("Failed to execute hooked action");
            }
        });
    }

    @Override
    public void portError(String errorMessage, Throwable error) {
        exceptionListeners.forEach(it -> it.accept(errorMessage, error));
    }

    /**
     * Wait for next phrase matching condition and return its result
     *
     * @return
     */
    public CompletableFuture<String> next(Predicate<String> condition) {
        //No need for synchronization since ReferenceRegistry is synchronized
        FuturePhrase res = new FuturePhrase(condition);
        waiters.add(res);
        res.whenComplete((r, e) -> waiters.remove(res));
        return res;
    }

    /**
     * Get next phrase matching pattern
     *
     * @param pattern
     * @return
     */
    public CompletableFuture<String> next(String pattern) {
        return next(it -> it.matches(pattern));
    }

    /**
     * Wait for any phrase
     *
     * @return
     */
    public CompletableFuture<String> next() {
        return next(str -> true);
    }

    /**
     * Block until specific phrase is received
     *
     * @param timeout
     * @param predicate
     * @return
     * @throws PortException
     */
    public String waitFor(Duration timeout, Predicate<String> predicate) {
        try {
            return next(predicate).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block thread until next phrase
     *
     * @param timeout
     * @return
     * @throws PortException
     */
    public String waitFor(Duration timeout) throws PortException {
        return waitFor(timeout, str -> true);
    }

    /**
     * Hook specific reaction to the specific phrase. Whenever it is possible, it is better to use {@code weakOnPhrase} to avoid memory leaks due to obsolete listeners.
     *
     * @param condition
     * @param action
     */
    public PhraseListener onPhrase(Predicate<String> condition, Consumer<String> action) {
        PhraseListener listener = new PhraseListener(condition, action);
        listeners.add(listener);
        return listener;
    }

    /**
     * Add weak phrase listener
     *
     * @param condition
     * @param action
     * @return
     */
    public PhraseListener weakOnPhrase(Predicate<String> condition, Consumer<String> action) {
        PhraseListener listener = new PhraseListener(condition, action);
        listeners.add(listener, false);
        return listener;
    }

    /**
     * Remove a specific phrase listener
     *
     * @param listener
     */
    public void removePhraseListener(PhraseListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Add action to phrase matching specific pattern
     *
     * @param pattern
     * @param action
     * @return
     */
    public PhraseListener onPhrase(String pattern, Consumer<String> action) {
        return onPhrase(it -> it.matches(pattern), action);
    }

    /**
     * Add reaction to any phrase
     *
     * @param action
     * @return
     */
    public PhraseListener onPhrase(Consumer<String> action) {
        return onPhrase(it -> true, action);
    }

    /**
     * Add error listener
     *
     * @param listener
     * @return
     */
    public BiConsumer<String, Throwable> onError(BiConsumer<String, Throwable> listener) {
        this.exceptionListeners.add(listener);
        return listener;
    }

    /**
     * Add weak error listener
     *
     * @param listener
     * @return
     */
    public BiConsumer<String, Throwable> weakOnError(BiConsumer<String, Throwable> listener) {
        this.exceptionListeners.add(listener, false);
        return listener;
    }

    /**
     * remove specific error listener
     *
     * @param listener
     */
    public void removeErrorListener(BiConsumer<String, Throwable> listener) {
        this.exceptionListeners.remove(listener);
    }

    /**
     * Send async message to port
     *
     * @param message
     */
    public void send(String message) {
        try {
            port.send(this, message);
        } catch (PortException e) {
            throw new RuntimeException("Failed to send message to port " + port);
        }
    }

    /**
     * Send and return the future with the result
     *
     * @param message
     * @param condition
     */
    public CompletableFuture<String> sendAndGet(String message, Predicate<String> condition) {
        send(message);
        return next(condition);
    }

    /**
     * Send and block thread until specific result is obtained. All listeners and reactions work as usual.
     *
     * @param message
     * @param duration
     * @param condition
     * @return
     */
    public String sendAndWait(String message, Duration duration, Predicate<String> condition) {
        send(message);
        return waitFor(duration, condition);
    }


    /**
     * Get the port associated with this controller
     *
     * @return
     */
    public Port getPort() {
        return port;
    }

    /**
     * Cancel all pending waiting actions and release the port. Does not close the port
     */
    @Override
    public void close() {
        waiters.forEach(it -> it.cancel(true));
        port.releaseBy(this);
    }

    private class FuturePhrase extends CompletableFuture<String> {
        final Predicate<String> condition;

        private FuturePhrase(Predicate<String> condition) {
            this.condition = condition;
        }

        void acceptPhrase(String phrase) {
            if (condition.test(phrase)) {
                complete(phrase);
            }
        }
    }

    public class PhraseListener {
        final Predicate<String> condition;
        final Consumer<String> action;

        public PhraseListener(Predicate<String> condition, Consumer<String> action) {
            this.condition = condition;
            this.action = action;
        }

        void acceptPhrase(String phrase) {
            if (condition.test(phrase)) {
                action.accept(phrase);
            }
        }
    }
}

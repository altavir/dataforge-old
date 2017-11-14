package hep.dataforge.control.ports;

import hep.dataforge.exceptions.ControlException;
import hep.dataforge.exceptions.PortException;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * A transitive port controller helper which allows for synchronous message processing
 */
public class SyncPortController implements PortHandler.PortController, AutoCloseable {

    /**
     * Use temporary controller to safely send request and recieve response
     * @param port
     * @param request
     * @param timeout
     * @return
     * @throws ControlException
     */
    public static String sendAndWait(PortHandler port, String request, Duration timeout) throws ControlException {
        SyncPortController controller = new SyncPortController(null);
        port.holdBy(controller);
        try{
            port.send(request);
            return controller.waitFor(timeout);
        } finally {
            port.unholdBy(controller);
        }
    }


    private final PortHandler.PortController delegate;

    private Set<WaitTask> waiters = new HashSet<>();

    public SyncPortController(PortHandler.PortController delegate) {
        this.delegate = delegate;
    }

    @Override
    public void acceptPortPhrase(String message) {
        if(delegate!= null) {
            delegate.acceptPortPhrase(message);
        }
        waiters.forEach(waitTask -> waitTask.acceptPhrase(message));
    }

    @Override
    public void portError(String errorMessage, Throwable error) {
        if(delegate!= null) {
            delegate.portError(errorMessage, error);
        }
        waiters.forEach(waitTask -> waitTask.completeExceptionally(error));
    }

    /**
     * remove completed waiters
     */
    private void cleanup() {
        waiters.removeIf(CompletableFuture::isDone);
    }

    /**
     * Wait for next phrase and return its result
     *
     * @return
     */
    public synchronized CompletableFuture<String> next(Predicate<String> condition) {
        cleanup();
        WaitTask res = new WaitTask(condition);
        waiters.add(res);
        return res;
    }

    /**
     * Wait for specific phrase ignoring anything else
     *
     * @return
     */
    public CompletableFuture<String> next() {
        return next(str -> true);
    }

    /**
     * Block until specific phrase is received
     * @param timeout
     * @param predicate
     * @return
     * @throws PortException
     */
    public String waitFor(Duration timeout, Predicate<String> predicate) throws PortException {
        try {
            return next(predicate).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PortException(e);
        }
    }

    /**
     * Block thread until next phrase
     * @param timeout
     * @return
     * @throws PortException
     */
    public String waitFor(Duration timeout) throws PortException {
        return waitFor(timeout, str -> true);
    }

    @Override
    public void close() throws Exception {
        waiters.forEach(it -> it.cancel(true));
    }

    private class WaitTask extends CompletableFuture<String> {
        final Predicate<String> condition;

        private WaitTask(Predicate<String> condition) {
            this.condition = condition;
        }

        void acceptPhrase(String phrase) {
            if (condition.test(phrase)) {
                complete(phrase);
            }
        }
    }
}

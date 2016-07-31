/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractGoal<T> implements Goal<T> {

    private final ExecutorService executor;
    private CompletableFuture<?> computation;
    private final CompletableFuture<T> result = new GoalResult<>();
    private Runnable onStart;
    private Thread thread;

    public AbstractGoal(ExecutorService executor) {
        this.executor = executor;
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public synchronized void start() {
        if (!isStarted()) {
            //start all dependencies so they will occupy threads
            computation = CompletableFuture
                    .allOf(depencencies()
                            .map(dep -> {
                                dep.start();//starting all dependencies
                                return dep.result();
                            })
                            .toArray(num -> new CompletableFuture[num]))
                    .thenAcceptAsync(v -> {
                        try {
                            thread = Thread.currentThread();
                            if(onStart!= null){
                                onStart.run();
                            }
                            this.result.complete(compute());
                            thread = null;
                        } catch (Exception ex) {
                            this.result.completeExceptionally(ex);
                        }
                    }, executor);
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    
    public void onStart(Runnable run){
        this.onStart = run;
    }

    protected abstract T compute() throws Exception;

    /**
     * Abort internal computation process without canceling result. Use with
     * care
     */
    protected void abort() {
        if (isStarted()) {
            if (this.computation != null) {
                this.computation.cancel(true);
            }
            if (thread != null) {
                thread.interrupt();
            }
        }
    }

    protected boolean isStarted() {
        return this.result.isDone() || this.computation != null;
    }

    /**
     * Abort current computation if it is in progress and set result. Useful for
     * caching purposes.
     *
     * @param result
     */
    @Override
    public final synchronized void complete(T result) {
        abort();
        this.result.complete(result);
    }

    @Override
    public CompletableFuture<T> result() {
        return result;
    }

    protected class GoalResult<T> extends CompletableFuture<T> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) {
                abort();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }

}

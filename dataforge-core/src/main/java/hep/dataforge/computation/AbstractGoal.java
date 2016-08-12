/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractGoal<T> implements Goal<T> {

    private final ExecutorService executor;
    private final CompletableFuture<T> result;
    private final List<Runnable> onStartHooks = new ArrayList<>();
    private final List<BiConsumer<? super T, ? super Throwable>> onCompleteHooks = new ArrayList<>();
    private CompletableFuture<?> computation;
    private Thread thread;

    public AbstractGoal(ExecutorService executor) {
        this.executor = executor;
        result = new GoalResult();//.whenComplete((res,err)-> onCompleteHooks.forEach(hook -> hook.accept(res, err)));
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public synchronized void start() {
        if (!isStarted()) {
            //start all dependencies so they will occupy threads
            computation = CompletableFuture
                    .allOf(dependencies()
                            .map(dep -> {
                                dep.start();//starting all dependencies
                                return dep.result();
                            })
                            .<CompletableFuture<?>>toArray(num -> new CompletableFuture[num]))
                    .thenAcceptAsync(v -> {
                        try {
                            thread = Thread.currentThread();
                            //trigger start hooks
                            onStartHooks.forEach(action -> action.run());
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

    public synchronized void onStart(Runnable hook) {
        this.onStartHooks.add(hook);
    }

    public synchronized void onComplete(BiConsumer<? super T, ? super Throwable> hook) {
        this.onCompleteHooks.add(hook);
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

    protected class GoalResult extends CompletableFuture<T> {

        @Override
        public boolean complete(T value) {
            onCompleteHooks.forEach(hook -> hook.accept(value, null));
            return super.complete(value);
        }

        @Override
        public boolean completeExceptionally(Throwable ex) {
            onCompleteHooks.forEach(hook -> hook.accept(null, ex));
            return super.completeExceptionally(ex);
        }


        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) {
                abort();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }

}

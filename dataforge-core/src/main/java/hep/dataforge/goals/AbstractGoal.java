/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

import hep.dataforge.utils.ReferenceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractGoal<T> implements Goal<T> {

    private final ReferenceRegistry<GoalListener<T>> listeners = new ReferenceRegistry<>();

    private final Executor executor;
    private final CompletableFuture<T> result;
    private CompletableFuture<?> computation;
    private Thread thread;

    public AbstractGoal(Executor executor) {
        this.executor = executor;
        result = new GoalResult();//.whenComplete((res,err)-> onCompleteHooks.forEach(hook -> hook.accept(res, err)));
    }

    public AbstractGoal() {
        this.executor = ForkJoinPool.commonPool();
        result = new GoalResult();//.whenComplete((res,err)-> onCompleteHooks.forEach(hook -> hook.accept(res, err)));
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public synchronized void run() {
        if (!isStarted()) {
            //start all dependencies so they will occupy threads
            computation = CompletableFuture
                    .allOf(dependencies()
                            .map(dep -> {
                                dep.run();//starting all dependencies
                                return dep.result();
                            })
                            .toArray(num -> new CompletableFuture[num]))
                    .whenCompleteAsync((res, err) -> {
                        if (err != null) {
                            this.result.completeExceptionally(err);
                        }

                        try {
                            thread = Thread.currentThread();
                            //trigger start hooks
                            listeners.forEach(listener -> listener.onGoalStart());
                            T r = compute();
                            //triggering result hooks
                            listeners.forEach(listener -> listener.onGoalComplete(getExecutor(), r));
                            this.result.complete(r);
                        } catch (Exception ex) {
                            //trigger exception hooks
                            listeners.forEach(listener -> listener.onGoalFailed(getExecutor(), ex));
                            this.result.completeExceptionally(ex);
                        } finally {
                            thread = null;
                        }
                    }, executor);
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    protected abstract T compute() throws Exception;

    /**
     * Abort internal goals process without canceling result. Use with
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
     * Abort current goals if it is in progress and set result. Useful for
     * caching purposes.
     *
     * @param result
     */
    public final synchronized boolean complete(T result) {
        abort();
        return this.result.complete(result);
    }

    @Override
    public CompletableFuture<T> result() {
        return result;
    }

    @Override
    public void registerListener(GoalListener<T> listener) {
        listeners.add(listener);
    }

    protected class GoalResult extends CompletableFuture<T> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) {
                abort();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }

}

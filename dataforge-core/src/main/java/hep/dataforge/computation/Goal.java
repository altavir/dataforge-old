/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A simple link in a computation chain
 *
 * @author Alexander Nozik
 */
public interface Goal<T> extends RunnableFuture<T> {

    /**
     * A stream of all bound direct dependencies
     *
     * @return
     */
    Stream<Goal> dependencies();

    /**
     * Start this goal computation. Triggers start of dependent goals
     */
    void run();

    /**
     * Get goal result for given slot. Does not trigger goal computation.
     * Canceling this future aborts all subsequent goals
     *
     * @return
     */
    CompletableFuture<T> result();

    /**
     * Start and get sync result
     *
     * @return
     */
    default T get() throws ExecutionException, InterruptedException {
        run();
        return result().get();
    }

    @Override
    default boolean cancel(boolean mayInterruptIfRunning) {
        return result().cancel(mayInterruptIfRunning);
    }

    @Override
    default boolean isCancelled() {
        return result().isCancelled();
    }

    @Override
    default boolean isDone() {
        return result().isDone();
    }

    @Override
    default T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result().get(timeout, unit);
    }

    /**
     * Complete this goal externally with given result. May or may not cancel
     * current execution
     *
     * @param result
     */
    default boolean complete(T result) {
        return result().complete(result);
    }


    void onStart(Runnable hook);

    void onComplete(BiConsumer<? super T, ? super Throwable> hook);
}

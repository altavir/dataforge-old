/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

import hep.dataforge.context.Global;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * An elementary lazy calculation which could be linked into a chain. By default, Goal wraps a CompletableFuture which is triggered with {@code run} method.
 *
 * @author Alexander Nozik
 */
public interface Goal<T> extends RunnableFuture<T> {

    /**
     * A stream of all bound direct dependencies
     *
     * @return
     */
    Stream<Goal<?>> dependencies();

    /**
     * Start this goal goals. Triggers start of dependent goals
     */
    void run();

    /**
     * The encapsulated {@link CompletableFuture}. Used to build goal chains.
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
    default T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result().get(timeout, unit);
    }

    /**
     * Register a listener. Result listeners are triggered before {@code result} is set so they will be evaluated before any subsequent goals are started.
     *
     * @param listener
     */
    void registerListener(GoalListener<T> listener);

    /**
     * Add on start hook which is executed in goal thread
     *
     * @param r
     */
    default void onStart(Runnable r) {
        onStart(Global.instance().singleThreadExecutor(), r);
    }

    /**
     * Add on start hook which is executed using custom executor
     *
     * @param executor
     * @param r
     */
    default void onStart(Executor executor, Runnable r) {
        registerListener(new GoalListener<T>() {
            @Override
            public void onGoalStart() {
                executor.execute(r);
            }

        });
    }

    /**
     * Handle results using goal executor.
     *
     * @param consumer
     */
    default void onComplete(BiConsumer<T, Throwable> consumer) {
        onComplete(Global.instance().singleThreadExecutor(), consumer);
    }

    /**
     * handle using custom executor
     *
     * @param exec
     * @param consumer
     */
    default void onComplete(Executor exec, BiConsumer<T, Throwable> consumer) {
        registerListener(new GoalListener<T>() {
            @Override
            public void onGoalComplete(T result) {
                exec.execute(() -> consumer.accept(result, null));
            }

            @Override
            public void onGoalFailed(Throwable ex) {
                exec.execute(() -> consumer.accept(null, ex));
            }
        });
    }
}

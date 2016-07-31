/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * A simple link in a computation chain
 *
 * @author Alexander Nozik
 */
public interface Goal<T> {

    /**
     * A stream of all bound direct dependencies
     *
     * @return
     */
    Stream<Goal> depencencies();

    /**
     * Start this goal computation. Triggers start of dependent goals
     */
    void start();

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
     * @throws Exception
     */
    default T get() throws Exception {
        start();
        return result().get();
    }

    /**
     * Complete this goal externally with given result. May or may not cancel
     * current execution
     *
     * @param result
     */
    default void complete(T result) {
        result().complete(result);
    }

}

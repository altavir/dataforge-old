/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.work;

import java.util.concurrent.CompletableFuture;

/**
 * A simple link in computation chain
 *
 * @author Alexander Nozik
 */
public interface Goal<T> {

    /**
     * Bind the output slot of given goal to input slot of this goal
     *
     * @param goal
     * @param outputSlot
     * @param inputSlot
     */
    void bindInput(Goal dependency, String inputSlot);

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

}

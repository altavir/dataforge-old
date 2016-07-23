/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


/**
 * Goal with a-priori known result
 * @author Alexander Nozik
 * @param <T> 
 */
public class StaticGoal<T> implements Goal<T> {
    
    private final CompletableFuture<T> future;

    public StaticGoal(T res) {
        this.future = CompletableFuture.completedFuture(res);
    }
    
    @Override
    public Stream<Goal> depencencies() {
        return Stream.empty();
    }

    @Override
    public void start() {
        // does nothing
    }

    @Override
    public CompletableFuture<T> result() {
        return future;
    }
    
}

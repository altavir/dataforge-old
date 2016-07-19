/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.LazyData;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A simple holder class that prevents any Future locked by it to run until it is
 * released.
 *
 * @author Alexander Nozik
 */
public class ActionController {

    CompletableFuture<Void> lock = new CompletableFuture<>();

    /**
     * Release all evaluations, which are locked by this hold
     */
    public void release() {
        lock.complete(null);
    }

    public CompletableFuture<Void> getLock() {
        return lock;
    }
    
    

    /**
     * Return held future. Actual parameter future could be already evaluated,
     * but all subsequent ones are held
     *
     * @param <R>
     * @param future
     * @return
     */
    public <R> CompletableFuture<R> hold(CompletableFuture<R> future) {
        return lock.thenCompose(res -> future);
    }

    /**
     * Return a held supplier
     *
     * @param <R>
     * @param sup
     * @return
     */
    public <R> CompletableFuture<R> hold(Supplier<R> sup) {
        return lock.thenApply(v -> sup.get());
    }

    public <R> Data<R> hold(Data<R> data) {
        return new LazyData<>(data.dataType(), hold(data.get()));
    }

    public <R> DataNode<R> hold(DataNode<R> node) {
        DataTree.Builder<R> builder = new DataTree.Builder<>(node);
        node.dataStream().forEach(pair -> {
            builder.putData(pair.getKey(), hold(pair.getValue()), true);
        });
        return builder.build();
    }
}

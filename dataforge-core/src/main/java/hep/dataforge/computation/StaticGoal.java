/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.stream.Stream;


/**
 * Goal with a-priori known result
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class StaticGoal<T> extends AbstractGoal<T> {
    private final T result;

    public StaticGoal(T result) {
        this.result = result;
    }

    @Override
    public Stream<Goal> dependencies() {
        return Stream.empty();
    }

    @Override
    protected T compute() throws Exception {
        return result;
    }

//    private final CompletableFuture<T> future;
//
//    public StaticGoal(T res) {
//        this.future = CompletableFuture.completedFuture(res);
//    }
//
//    @Override
//    public Stream<Goal> dependencies() {
//        return Stream.empty();
//    }
//
//    @Override
//    public void run() {
//        // does nothing
//    }
//
//    @Override
//    public CompletableFuture<T> result() {
//        return future;
//    }
//
//    @Override
//    public void onStart(Runnable hook) {
//        //this goal never starts
//    }
//
//    @Override
//    public void onComplete(BiConsumer<? super T, ? super Throwable> hook) {
//        //run immediately on separate thread
//        new Thread(() -> hook.accept(future.join(), null)).start();
//
//    }
}

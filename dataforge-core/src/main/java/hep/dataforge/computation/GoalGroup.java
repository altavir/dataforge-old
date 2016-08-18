/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A goal with no result which is completed when all its dependencies are
 * completed. Stopping this goal does not stop dependencies.
 *
 * @author Alexander Nozik
 */
public class GoalGroup extends AbstractGoal<Void> {
    private final Collection<Goal> dependencies;

    public GoalGroup(Collection<Goal> dependencies) {
        this.dependencies = dependencies;
    }


    @Override
    public Stream<Goal> dependencies() {
        return dependencies.stream();
    }

    @Override
    protected Void compute() throws Exception {
        return null;
    }

//    private final Collection<Goal> dependencies;
//    private final CompletableFuture<Void> future;
//
//    private final List<Runnable> onStartHooks = new ArrayList<>();
//    private final List<BiConsumer<? super Void, ? super Throwable>> onCompleteHooks = new ArrayList<>();
//
//    public GoalGroup(Collection<Goal> dependencies) {
//        this.dependencies = dependencies;
//        this.future = CompletableFuture.allOf(dependencies.stream()
//                .map(dep -> dep.result()).<CompletableFuture<?>>toArray(num -> new CompletableFuture[num]))
//                .whenCompleteAsync((res, err) -> onCompleteHooks.forEach((hook) -> hook.accept(res, err)));
//    }
//
//    @Override
//    public Stream<Goal> dependencies() {
//        return dependencies.stream();
//    }
//
//    @Override
//    public void run() {
//        dependencies.stream().forEach(dep -> dep.run());
//        onStartHooks.forEach((hook) -> hook.run());
//    }
//
//    @Override
//    public CompletableFuture<Void> result() {
//        return future;
//    }
//
//    @Override
//    public void onStart(Runnable hook) {
//        onStartHooks.add(hook);
//    }
//
//    @Override
//    public void onComplete(BiConsumer<? super Void, ? super Throwable> hook) {
//        onCompleteHooks.add(hook);
//    }
}

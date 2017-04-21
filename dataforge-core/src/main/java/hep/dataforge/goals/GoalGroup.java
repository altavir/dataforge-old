/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

import hep.dataforge.utils.ReferenceRegistry;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A goal with no result which is completed when all its dependencies are
 * completed. Stopping this goal does not stop dependencies. Staring goal does start dependencies.
 *
 * On start hooks works only if this group was specifically started. All of its dependencies could be started and completed without triggering it.
 *
 * @author Alexander Nozik
 */
public class GoalGroup implements Goal<Void> {
    private final ReferenceRegistry<GoalListener> listeners = new ReferenceRegistry<>();
    private final Collection<Goal> dependencies;
    private final CompletableFuture<Void> res;

    public GoalGroup(Collection<Goal> dependencies) {
        this.dependencies = dependencies;
        res = CompletableFuture
                .allOf(dependencies.stream().map(dep -> dep.result()).toArray(i -> new CompletableFuture<?>[i]))
                .whenComplete(new BiConsumer<Void, Throwable>() {
                    @Override
                    public void accept(Void aVoid, Throwable throwable) {
                        if (throwable != null) {
                            listeners.forEach(l -> l.onGoalFailed(throwable));
                        } else {
                            listeners.forEach(l -> l.onGoalComplete(null));
                        }
                    }
                });
    }

    @Override
    public Stream<Goal> dependencies() {
        return dependencies.stream();
    }

    @Override
    public void run() {
        listeners.forEach(l -> l.onGoalStart());
        dependencies.forEach(it -> it.run());
    }

    @Override
    public CompletableFuture<Void> result() {
        return res;
    }

    @Override
    public void registerListener(GoalListener<Void> listener) {
        listeners.add(listener);
    }



}

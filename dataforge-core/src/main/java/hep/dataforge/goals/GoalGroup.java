/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

import hep.dataforge.utils.ReferenceRegistry;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * A goal with no result which is completed when all its dependencies are
 * completed. Stopping this goal does not stop dependencies. Staring goal does start dependencies.
 * <p>
 * On start hooks works only if this group was specifically started. All of its dependencies could be started and completed without triggering it.
 *
 * @author Alexander Nozik
 */
public class GoalGroup implements Goal<Void> {
    private final ReferenceRegistry<GoalListener<?>> listeners = new ReferenceRegistry<>();
    private final Collection<Goal<?>> dependencies;
    private final CompletableFuture<Void> res;

    public GoalGroup(Collection<Goal<?>> dependencies) {
        this.dependencies = dependencies;
        res = CompletableFuture
                .allOf(dependencies.stream().map(Goal::result).toArray((IntFunction<CompletableFuture<?>[]>) CompletableFuture[]::new))
                .whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        listeners.forEach(l -> l.onGoalFailed(throwable));
                    } else {
                        listeners.forEach(l -> l.onGoalComplete(null));
                    }
                });
    }

    @Override
    public Stream<Goal<?>> dependencies() {
        return dependencies.stream();
    }

    @Override
    public void run() {
        listeners.forEach(GoalListener::onGoalStart);
        dependencies.forEach(Goal::run);
    }

    @Override
    public CompletableFuture<Void> result() {
        return res;
    }

    @Override
    public boolean isRunning() {
        return dependencies.stream().anyMatch(Goal::isRunning);
    }

    @Override
    public void registerListener(GoalListener<Void> listener) {
        listeners.add(listener, true);
    }


}

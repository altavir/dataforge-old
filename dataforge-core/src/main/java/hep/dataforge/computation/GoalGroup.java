/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * A goal with no result which is completed when all its dependencies are
 * completed. Stopping this goal does not stop dependencies.
 *
 * @author Alexander Nozik
 */
public class GoalGroup implements Goal<Void> {

    private final Collection<Goal> dependencies;
    private final CompletableFuture<Void> future;

    public GoalGroup(Collection<Goal> dependencies) {
        this.dependencies = dependencies;
        this.future = CompletableFuture.allOf(dependencies.stream().map(dep -> dep.result()).toArray(num -> new CompletableFuture[num]));
    }

    @Override
    public Stream<Goal> depencencies() {
        return dependencies.stream();
    }

    @Override
    public void start() {
        dependencies.stream().forEach(dep -> dep.start());
    }

    @Override
    public CompletableFuture<Void> result() {
        return future;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.goals;

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
}

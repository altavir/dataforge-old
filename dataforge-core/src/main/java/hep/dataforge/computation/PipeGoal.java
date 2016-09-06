/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A one-to one pipeline goal
 *
 * @author Alexander Nozik
 * @param <S>
 * @param <T>
 */
public class PipeGoal<S, T> extends AbstractGoal<T> {

    private final Goal<S> source;
    private final Function<S, T> transfromation;

    public PipeGoal(Goal<S> source, Executor executor, Function<S, T> transfromation) {
        super(executor);
        this.source = source;
        this.transfromation = transfromation;
    }

    public PipeGoal(Goal<S> source, Function<S, T> transfromation) {
        this.source = source;
        this.transfromation = transfromation;
    }

    @Override
    protected T compute() throws Exception {
        return transfromation.apply(source.get());
    }

    @Override
    public Stream<Goal> dependencies() {
        return Stream.of(source);
    }

    /**
     * Attach new pipeline goal to this one using same executor
     *
     * @param <R>
     * @param trans
     * @return
     */
    public <R> PipeGoal<T, R> andThen(Function<T, R> trans) {
        return new PipeGoal<>(this, getExecutor(), trans);
    }

}

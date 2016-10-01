/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.query;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A query using java 8 stream
 *
 * @author Alexander Nozik
 * @param <T>
 */
@Deprecated
public class StreamQuery<T> implements Query<T> {

    private final Supplier<Stream<T>> sup;
    private Predicate<T> condition;
    private Function<Stream<T>, Stream<T>> transformation;

    public StreamQuery(Supplier<Stream<T>> sup) {
        this.sup = sup;
    }

    protected StreamQuery(Supplier<Stream<T>> sup, Predicate<T> condition, Function<Stream<T>, Stream<T>> transformation) {
        this.sup = sup;
        if (condition == null) {
            throw new IllegalArgumentException("Predicate is null");
        } else {
            this.condition = condition;
        }
        this.transformation = transformation;
    }

    protected StreamQuery buildQuery(Supplier<Stream<T>> sup, Predicate<T> condition, Function<Stream<T>, Stream<T>> transformation) {
        return new StreamQuery(sup, condition, transformation);
    }

    /**
     * Create a filtering query or narrow existing one
     * @param predicate
     * @return 
     */
    public StreamQuery filter(Predicate<T> predicate) {
        if (this.condition == null) {
            return buildQuery(sup, predicate, transformation);
        } else {
            return buildQuery(sup, condition.and(predicate), transformation);
        }
    }

    /**
     * Create a after filter transformation or append transformation to existing one
     * @param function
     * @return 
     */
    public StreamQuery transform(Function<Stream<T>, Stream<T>> function) {
        if (this.condition == null) {
            return buildQuery(sup, condition, function);
        } else {
            return buildQuery(sup, condition, transformation.andThen(function));
        }
    }

    @Override
    public Stream<T> makeStream(){
        Stream<T> stream = sup.get();
        if (condition != null) {
            stream = stream.filter(condition);
        }
        if (transformation != null) {
            stream = transformation.apply(stream);
        }
        return stream;
    }
    
    @Override
    public Collection<T> make() {
        return makeStream().collect(Collectors.toList());
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueUtils;
import javafx.util.Pair;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The simple index, which uses item number for search
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class DefaultIndex<T> implements ValueIndex<T> {

    private final Iterable<T> iterable;

    public DefaultIndex(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public Stream<Pair<Integer, T>> stream() {
        AtomicInteger counter = new AtomicInteger(0);
        return StreamSupport.stream(iterable.spliterator(), false).map(it -> new Pair<>(counter.getAndIncrement(), it));
    }

    @Override
    public Stream<T> pull(Value value) throws StorageException {
        return stream().filter(pair -> value.intValue() == pair.getKey())
                .map(Pair::getValue);
    }

    @Override
    public Stream<T> pull(Value from, Value to) throws StorageException {
        return stream().filter(pair -> ValueUtils.isBetween(pair.getKey(), from, to))
                .map(Pair::getValue);
    }

    public Stream<T> pull(Predicate<Integer> predicate) {
        return stream().filter(t -> predicate.test(t.getKey()))
                .map(Pair::getValue);
    }

    @Override
    public NavigableSet<Value> keySet() {
        TreeSet<Value> res = new TreeSet<>();
        stream().forEach(it -> res.add(Value.of(it.getKey())));
        return res;
    }
}

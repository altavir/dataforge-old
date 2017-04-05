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

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The simple index, which uses item number for search
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class DefaultIndex<T> implements ValueIndex<T>, Iterable<Pair<Integer, T>> {

    private final Iterable<T> iterable;

    public DefaultIndex(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<Pair<Integer, T>> iterator() {
        Iterator<T> iterator = iterable.iterator();
        return new Iterator<Pair<Integer, T>>() {
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Pair<Integer, T> next() {
                Pair<Integer, T> res = new Pair<>(counter, iterator.next());
                counter++;
                return res;
            }
        };
    }


    @Override
    public Stream<T> pull(Value value) throws StorageException {
        return StreamSupport.stream(spliterator(), true)
                .filter(pair -> value.intValue() == pair.getKey())
                .map(pair -> pair.getValue());
    }

    @Override
    public Stream<T> pull(Value from, Value to) throws StorageException {
        return StreamSupport.stream(spliterator(), true)
                .filter(pair -> ValueUtils.isBetween(pair.getKey(), from, to))
                .map(pair -> pair.getValue());
    }

    public Stream<T> pull(Predicate<Integer> predicate) {
        return StreamSupport.stream(spliterator(), true)
                .filter(t -> predicate.test(t.getKey()))
                .map(pair -> pair.getValue());
    }

    @Override
    public NavigableSet<Value> keySet() {
        TreeSet<Value> res = new TreeSet<>();
        StreamSupport.stream(spliterator(), false).forEach(it -> res.add(Value.of(it.getKey())));
        return res;
    }
}

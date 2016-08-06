/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The base for uncached index. Uses streams to filter required conditions. Each
 * pull operation starts search from the start and does not use an caching. So
 * it is better used for single time searches.
 *
 * @author Alexander Nozik
 */
public abstract class StreamIndex<K, T> implements ValueIndex<T>, Iterable<K> {

    @Override
    public abstract Iterator<K> iterator();

    protected abstract Value getIndexedValue(T item);

    protected abstract T transform(K item);

    @Override
    public List<T> pull(Value value) throws StorageException {
        return pull(k -> getIndexedValue(k).equals(value));
    }

    @Override
    public List<T> pull(Value from, Value to) throws StorageException {
        return pull(k -> ValueUtils.isBetween(k, from, to));
    }

    @Override
    public List<T> pull(Value from, Value to, int maxItems) throws StorageException {
        return StreamSupport.stream(spliterator(), true)
                .<T>map(k -> transform(k))
                .filter(t -> ValueUtils.isBetween(getIndexedValue(t), from, to))
                .limit(maxItems)
                .collect(Collectors.toList());
    }

    public List<T> pull(Predicate<T> predicate) {
        return StreamSupport.stream(spliterator(), true)
                .<T>map(k -> transform(k))
                .filter(t -> predicate.test(t))
                .collect(Collectors.toList());
    }

}

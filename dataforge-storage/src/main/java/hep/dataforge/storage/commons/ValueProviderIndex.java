/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.ValueUtils;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple in memory index. Very inefficient.
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class ValueProviderIndex<T extends ValueProvider> implements ValueIndex<T> {

    private final String valueName;
    private final Value defaultValue;
    private final Iterable<T> iterable;

    public ValueProviderIndex(Iterable<T> iterable, String valueName) {
        this.iterable = iterable;
        this.valueName = valueName;
        defaultValue = Value.NULL;
    }

    /**
     * @param iterable
     * @param valueName
     * @param defaultValue the default value in case some of iterated items does
     *                     not provide required name
     */
    public ValueProviderIndex(Iterable<T> iterable, String valueName, Value defaultValue) {
        this.iterable = iterable;
        this.valueName = valueName;
        this.defaultValue = defaultValue;
    }

    @Override
    public Stream<T> pull(Value value) throws StorageException {
        return StreamSupport.stream(iterable.spliterator(), true)
                .filter(it -> it.getValue(valueName, defaultValue).equals(value));
    }

    @Override
    public Stream<T> pull(Value from, Value to) throws StorageException {
        return StreamSupport.stream(iterable.spliterator(), true)
                .filter(it -> ValueUtils.isBetween(it.getValue(valueName, defaultValue), from, to));
    }

    @Override
    public NavigableSet<Value> keySet() {
        TreeSet<Value> res = new TreeSet<>(ValueUtils.VALUE_COMPARATPR);
        StreamSupport.stream(iterable.spliterator(),true).map(it-> it.getValue(valueName, defaultValue)).forEach(it-> res.add(it));
        return res;
    }
}

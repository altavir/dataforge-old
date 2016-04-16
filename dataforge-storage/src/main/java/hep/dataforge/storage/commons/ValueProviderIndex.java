/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.util.Iterator;

/**
 * Simple StreamIndex for value providers
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class ValueProviderIndex<T extends ValueProvider> extends StreamIndex<T, T> {

    private final String valueName;
    private final Value defaultValue;
    private final Iterable<T> iterable;

    public ValueProviderIndex(Iterable<T> iterable, String valueName) {
        this.iterable = iterable;
        this.valueName = valueName;
        defaultValue = Value.NULL;
    }

    /**
     *
     * @param iterable
     * @param valueName
     * @param defaultValue the default value in case some of iterated items does
     * not provide required name
     */
    public ValueProviderIndex(Iterable<T> iterable, String valueName, Value defaultValue) {
        this.iterable = iterable;
        this.valueName = valueName;
        this.defaultValue = defaultValue;
    }

    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }

    /**
     * Get named field
     *
     * @param item
     * @return
     */
    @Override
    protected Value getIndexedValue(T item) {
        return item.getValue(valueName, defaultValue);
    }

    /**
     * Identity transformation
     *
     * @param item
     * @return
     */
    @Override
    protected T transform(T item) {
        return item;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.values.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A fast access index based on treeMap. It needs to be updated prior to any use
 * and requires a lot of memory, so it should be used only in cases of repeated
 * pull requests.
 *
 * @param K intermediate key representation for entries.
 * @author Alexander Nozik
 */
public abstract class MapIndex<T, K> implements ValueIndex<T> {

    protected TreeMap<Value, List<K>> map = new TreeMap<>();

    /**
     * Store index entry
     *
     * @param v
     * @param key
     */
    protected void putToIndex(Value v, K key) {
        if (!map.containsKey(v)) {
            map.put(v, new ArrayList<>());
        }
        map.get(v).add(key);
    }

    /**
     * Get stored value by key. Could use some external information.
     *
     * @param key
     * @return
     */
    protected abstract T transform(K key);

    protected abstract Value getIndexedValue(T entry);

//    /**
//     * Get index for specified entry
//     *
//     * @param key
//     * @return
//     */
//    protected abstract Value getIndex(K key);

    /**
     * Update index to match source
     */
    protected abstract void update() throws StorageException;

    private List<Supplier<T>> transform(List<K> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().<Supplier<T>>map(k -> () -> transform(k)).collect(Collectors.toList());
    }

    @Override
    public List<Supplier<T>> pull(Value value) throws StorageException {
        update();
        return transform(map.get(value));
    }

    @Override
    public List<Supplier<T>> pull(Value from, Value to) throws StorageException {
        update();
        List<Supplier<T>> res = new ArrayList();
        map.subMap(from, true, to, true).forEach((Value t, List<K> u) -> {
            res.addAll(transform(u));
        });
        return res;
    }




    public void invalidate() throws StorageException {
        this.map.clear();
    }

}

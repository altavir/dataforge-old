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

import java.util.*;
import java.util.stream.Stream;

/**
 * A fast access index based on treeMap. It needs to be updated prior to any use
 * and requires a lot of memory, so it should be used only in cases of repeated
 * pull requests.
 *
 * @param <K> intermediate key representation for entries.
 * @author Alexander Nozik
 */
public abstract class MapIndex<T, K> implements ValueIndex<T> {

    //TODO add custom request that fetches roots of the tree
    protected TreeMap<Value, List<K>> map = new TreeMap<>(ValueUtils.VALUE_COMPARATPR);

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

    /**
     * Update index to match source
     */
    protected abstract void update() throws StorageException;

//    private List<Supplier<T>> transform(List<K> list) {
//        if (list == null) {
//            return Collections.emptyList();
//        }
//        return list.stream().<Supplier<T>>map(k -> () -> transform(k)).collect(Collectors.toList());
//    }

    @Override
    public Stream<T> pull(Value value) throws StorageException {
        update();
        List<K> list = map.get(value);
        if(list == null){
            return Stream.empty();
        } else {
            return list.stream().map(this::transform);
        }
    }

    @Override
    public Optional<T> pullOne(Value value) throws StorageException {
        Map.Entry<Value, List<K>> entry = map.ceilingEntry(value);
        if (entry != null) {
            return entry.getValue().stream().map(this::transform).findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Stream<T> pull(Value from, Value to) throws StorageException {
        update();
        if(map.isEmpty()){
            return Stream.empty();
        }
        //If null, use the whole range
        if (from == Value.NULL) {
            from = map.firstKey();
        }

        if (to == Value.NULL) {
            to = map.lastKey();
        }


        return map.subMap(from, true, to, true).values().stream().flatMap(u -> u.stream().map(this::transform));
    }


    public void invalidate() throws StorageException {
        this.map.clear();
    }

    @Override
    public NavigableSet<Value> keySet() throws StorageException {
        update();
        return this.map.navigableKeySet();
    }
}

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    public Supplier<T> pullOne(Value value) throws StorageException {
        Map.Entry<Value, List<K>> entry = map.ceilingEntry(value);
        if (entry != null) {
            return transform(entry.getValue()).get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Supplier<T>> pull(Value from, Value to) throws StorageException {
        update();
        if(map.isEmpty()){
            return Collections.emptyList();
        }
        //If null, use the whole range
        if (from == Value.NULL) {
            from = map.firstKey();
        }

        if (to == Value.NULL) {
            to = map.lastKey();
        }

        List<Supplier<T>> res = new ArrayList();
        map.subMap(from, true, to, true).forEach((Value t, List<K> u) -> {
            res.addAll(transform(u));
        });
        return res;
    }


    public void invalidate() throws StorageException {
        this.map.clear();
    }

    @Override
    public NavigableSet<Value> keySet() throws StorageException {
        update();
        return this.map.navigableKeySet();
    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public List<Supplier<T>> pull(Value from, Value to, int limit) throws StorageException {
//        TreeMap<Value, List<K>> subMap;
//        if (from == Value.NULL && to == Value.NULL) {
//            subMap = map;
//        } else if (from == Value.NULL) {
//            subMap = (TreeMap<Value, List<K>>) map.tailMap(to);
//        } else if (to == Value.NULL) {
//            subMap = (TreeMap<Value, List<K>>) map.headMap(from);
//        } else {
//            subMap = (TreeMap<Value, List<K>>) map.subMap(from, to);
//        }
//
//
//        try {
//            return transform(top(subMap, limit));
//        } catch (Exception e) {
//            throw new StorageException(e);
//        }
//    }
//
//    /**
//     * get the top elements of the balanced tree map using reflections
//     *
//     * @param theMap
//     * @param limit
//     * @return
//     */
//    private List<K> top(TreeMap<Value, List<K>> theMap, int limit) throws NoSuchFieldException, IllegalAccessException {
//        Field field = theMap.getClass().getDeclaredField("root");
//        field.setAccessible(true);
//        Map.Entry<Value, List<K>> root = (Map.Entry<Value, List<K>>) field.get(theMap);
//        return top(root, limit);
//    }
//
//    private List<K> top(Map.Entry<Value, List<K>> theRoot, int limit) throws NoSuchFieldException, IllegalAccessException {
//
//        List<K> res = new ArrayList<K>();
//        //adding values from current node
//        for (K k : theRoot.getValue()) {
//            res.add(k);
//            // escape if reached limit
//            if (res.size() == limit) {
//                return res;
//            }
//        }
//
//        Field rightF = theRoot.getClass().getDeclaredField("right");
//        rightF.setAccessible(true);
//        Map.Entry<Value, List<K>> right = (Map.Entry<Value, List<K>>) rightF.get(theRoot);
//        Field leftF = theRoot.getClass().getDeclaredField("left");
//        leftF.setAccessible(true);
//        Map.Entry<Value, List<K>> left = (Map.Entry<Value, List<K>>) leftF.get(theRoot);
//
//        int rest = limit - res.size();
//
//        if (right == null && left == null) {
//            return res;
//        } else if (right == null) {
//            res.addAll(top(left, rest));
//        } else if (left == null) {
//            res.addAll(top(right, rest));
//        } else {
//            //adding values from right node
//            res.addAll(top(right, (rest - 1) / 2));
//            //adding values from left node
//            res.addAll(top(left, (rest - 1) / 2));
//        }
//        return res;
//    }
}

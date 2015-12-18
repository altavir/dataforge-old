/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.values.Value;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilities to work with meta
 *
 * @author Alexander Nozik
 */
public class MetaUtils {

    /**
     * Find all nodes with given path that satisfy given condition. Return empty
     * list if no such nodes are found.
     *
     * @param root
     * @param path
     * @param condition
     * @return
     */
    public static List<Meta> findNodes(Meta root, String path, Predicate<Meta> condition) {
        if (!root.hasNode(path)) {
            return Collections.emptyList();
        } else {
            return root.getNodes(path).stream()
                    .filter(condition)
                    .collect(Collectors.<Meta>toList());
        }
    }

    /**
     * Return the first node with given path that satisfies condition. Null if
     * no such nodes are found.
     *
     * @param root
     * @param path
     * @param condition
     * @return
     */
    public static Meta findNode(Meta root, String path, Predicate<Meta> condition) {
        List<? extends Meta> list = findNodes(root, path, condition);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * Find node by given key-value pair
     *
     * @param root
     * @param path
     * @param key
     * @param value
     * @return
     */
    public static Meta findNodeByValue(Meta root, String path, String key, Object value) {
        return findNode(root, path, (m) -> m.hasValue(key) && m.getValue(key).equals(Value.of(value)));
    }
}

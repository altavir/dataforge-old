/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 */
public class CommonUtils {
    /**
     * A synchronized lru cache
     * @param <K>
     * @param <V>
     * @param maxItems
     * @return 
     */
    public static <K,V> Map<K,V> getLRUCache(int maxItems){
        return Collections.synchronizedMap(new LinkedHashMap<K,V>(){
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return super.size() > maxItems;
            }
        });
    }
}

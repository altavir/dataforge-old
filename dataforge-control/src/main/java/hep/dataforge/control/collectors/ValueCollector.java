/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.collectors;

import hep.dataforge.values.Value;

/**
 * A collector of values which listens to some input values until condition
 * satisfied then pushes the result to external listener.
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public interface ValueCollector {

    public void put(String name, Value value);

    default public void put(String name, Object value) {
        put(name, Value.of(value));
    }

    /**
     * Send current cached result to listener. Could be used to force collect
     * even if not all values are present.
     */
    public void collect();
}

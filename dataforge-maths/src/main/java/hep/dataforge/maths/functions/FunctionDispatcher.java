/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.functions;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory combining other factories by type
 *
 * @author Alexander Nozik
 */
public class FunctionDispatcher<T> implements MetaFactory<T> {

    private final Map<String, MetaFactory<T>> factoryMap = new HashMap<>();

    @Override
    public T build(Meta meta) {
        String type = meta.getString("type", "");
        if (factoryMap.containsKey(type)) {
            return factoryMap.get(type).build(meta);
        } else {
            throw new NotDefinedException("Function with type '" + type + "' not defined");
        }
    }

    public synchronized FunctionDispatcher addFactory(String type, MetaFactory<T> factory) {
        this.factoryMap.put(type, factory);
        return this;
    }

}

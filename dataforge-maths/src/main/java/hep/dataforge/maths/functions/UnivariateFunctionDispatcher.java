/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.functions;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * A factory combining other factories by type
 * @author Alexander Nozik
 */
public class UnivariateFunctionDispatcher implements UnivariateFunctionFactory {

    private final Map<String, UnivariateFunctionFactory> factoryMap = new HashMap<>();

    @Override
    public UnivariateFunction build(Meta meta) {
        String type = meta.getString("type", "");
        if (factoryMap.containsKey(type)) {
            return factoryMap.get(type).build(meta);
        } else {
            throw new NotDefinedException("Function with type '" + type + "' not defined");
        }
    }

    public synchronized UnivariateFunctionDispatcher addFactory(String type, UnivariateFunctionFactory factory) {
        this.factoryMap.put(type, factory);
        return this;
    }

}

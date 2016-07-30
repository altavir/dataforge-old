/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fitting.parametric;

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;

public abstract class AbstractParametricBiFunction extends AbstractParametric implements ParametricBiFunction {

    public AbstractParametricBiFunction(Names names) {
        super(names);
    }

    public AbstractParametricBiFunction(String[] list) {
        super(list);
    }

    public AbstractParametricBiFunction(NameSetContainer set) {
        super(set);
    }

    @Override
    public double derivValue(String parName, double x, double y, NamedValueSet set) {
        if (!names().contains(parName)) {
            return 0;
        } else {
            throw new NotDefinedException();
        }
    }
    
}

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

/**
 *
 * @author Alexander Nozik
 */
public interface ParametricBiFunction extends NameSetContainer {

    double derivValue(String parName, double x, double y, NamedValueSet set);

    double value(double x, double y, NamedValueSet set);

    boolean providesDeriv(String name);

    default ParametricBiFunction derivative(String parName) {
        if (providesDeriv(parName)) {
            return new ParametricBiFunction() {
                @Override
                public double derivValue(String parName, double x, double y, NamedValueSet set) {
                    throw new NotDefinedException();
                }

                @Override
                public double value(double x, double y, NamedValueSet set) {
                    return ParametricBiFunction.this.derivValue(parName, x, y, set);
                }

                @Override
                public boolean providesDeriv(String name) {
                    return !names().contains(name);
                }

                @Override
                public Names names() {
                    return ParametricBiFunction.this.names();
                }
            };
        } else {
            throw new NotDefinedException();
        }
    }
}

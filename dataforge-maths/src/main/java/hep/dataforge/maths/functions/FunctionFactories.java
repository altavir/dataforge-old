/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.functions;

import hep.dataforge.utils.MetaFactory;
import hep.dataforge.values.Value;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 *
 * @author Alexander Nozik
 */
public class FunctionFactories {

    public static MetaFactory<UnivariateFunction> parabola() {
        return meta -> {
            double a = meta.getDouble("a", 1);
            double b = meta.getDouble("b", 0);
            double c = meta.getDouble("b", 0);
            return x -> a * x * x + b * x + c;
        };
    }

    public static MetaFactory<UnivariateFunction> polynomial() {
        return meta -> {
            List<Value> coefs = meta.getValue("coef").listValue();
            return x -> {
                double sum = 0;
                double curX = 1;
                for (int i = 0; i < coefs.size(); i++) {
                    sum += coefs.get(i).doubleValue()*curX;
                    curX = curX*x;
                }
                return sum;
            };
        };
    }
}

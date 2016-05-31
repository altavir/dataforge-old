/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.values.NamedValueSet;

/**
 *
 * @author Alexander Nozik
 */
public class MathUtils {

    public static double[] getDoubleArray(NamedValueSet set, String... names) {
        //fast access method for double vectors
        if (set instanceof NamedVector) {
            return ((NamedVector) set).getArray(names);
        }

        if (names.length == 0) {
            names = set.namesAsArray();
        }
        double[] res = new double[names.length];
        for (String name : names) {
            int index = set.names().getNumberByName(name);
            if (index < 0) {
                throw new NameNotFoundException(name);
            }
            res[index] = set.getDouble(name);
        }
        return res;
    }

    public static String toString(NamedValueSet set, String... names) {
        String res = "[";
        if (names.length == 0) {
            names = set.names().asArray();
        }
        boolean flag = true;
        for (String name : names) {
            if (flag) {
                flag = false;
            } else {
                res += ", ";
            }
            res += name + ":" + set.getDouble(name);
        }
        return res + "]";
    }
}

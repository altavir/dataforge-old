/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.io;

import hep.dataforge.datafitter.Param;
import hep.dataforge.datafitter.ParamSet;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import static java.util.Locale.setDefault;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Some IOUtils for String operations
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FittingIOUtils {

    /**
     * <p>getValueSet.</p>
     *
     * @param names a {@link java.lang.String} object.
     * @param doubles a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public static NamedDoubleSet getValueSet(String names, String doubles){
        Logger.getAnonymousLogger().warning("Using obsolete input method.");
        setDefault(Locale.ENGLISH);
        int i;
        try (Scanner sc = new Scanner(names)) {
            i = 0;
            sc.useDelimiter("\\p{Space}");
            while (sc.hasNext()) {
                sc.next();
                i++;
            }
        }
        String[] list = new String[i];
        double[] values = new double[i];
        Scanner sc1 = new Scanner(names);
        Scanner sc2 = new Scanner(doubles);
        sc1.useDelimiter("\\p{Space}");


        for (i = 0; i < list.length; i++) {
            list[i] = sc1.next();
            if (!sc2.hasNextDouble()) {
                throw new RuntimeException("Wrong input for ParamSet.");
            }
            values[i] = sc2.nextDouble();
        }
        NamedDoubleArray set = new NamedDoubleArray(list, values);
        return set;
    }

    /**
     * <p>printParamSet.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public static void printParamSet(PrintWriter out, ParamSet set){
        out.println();
       
        for (Param param: set.getParams()) {
            out.println(param.toString());
        }
    }
    
    /**
     * <p>scanParamSet.</p>
     *
     * @param reader a {@link java.util.Iterator} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public static ParamSet scanParamSet(Iterator<String> reader){
        String line = reader.next();
        Scanner scan;
        String str;

        Param par;
        ArrayList<Param> pars = new ArrayList<>();
        String name;
        double value;
        double err;
//        Double lower;
//        Double upper;

        Double lowerBound;
        Double upperBound;

        if (!line.startsWith("{")) {
            throw new RuntimeException("Syntax error. Line should begin with \'{\' ");
        }
        line = reader.next();
        while (!line.startsWith("}")) {
            scan = new Scanner(line);
            //           str = scan.next("*\t:");
            str = scan.findInLine("^.*:");
            if (str == null) {
                throw new RuntimeException("Syntax error. Wrong format for parameter definition.");
            }
            name = str.substring(str.indexOf('\'') + 1, str.lastIndexOf('\''));
            par = new Param(name);
            value = scan.nextDouble();
            par.setValue(value);

            if (scan.hasNextDouble()) {
                err = scan.nextDouble();
                par.setErr(err);
                if (scan.hasNextDouble()) {
                    lowerBound = scan.nextDouble();
                    upperBound = scan.nextDouble();
                } else {
                    lowerBound = Double.NEGATIVE_INFINITY;
                    upperBound = Double.POSITIVE_INFINITY;
                }
                par.setDomain(lowerBound, upperBound);
            }
            
            
            pars.add(par);
            line = reader.next();
        }

        int i;
        ParamSet res = new ParamSet();
        for (i = 0; i < pars.size(); i++) {
            res.setPar(pars.get(i));
        }

        return res;

    }

    
}

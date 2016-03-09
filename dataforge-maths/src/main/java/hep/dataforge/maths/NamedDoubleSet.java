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
package hep.dataforge.maths;

import hep.dataforge.names.NameSet;

/**
 * <p>NamedDoubleSet interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
//TODO заменить везде все на Value
public interface NamedDoubleSet extends NameSet {

    /**
     * <p>getValue.</p>
     *
     * @param string a {@link java.lang.String} object.
     * @return a double.
     */
    double getValue(String string);

    /**
     * <p>getValues.</p>
     *
     * @param names a {@link java.lang.String} object.
     * @return an array of double.
     */
    double[] getValues(String... names);
    
        
    /**
     * <p>toString.</p>
     *
     * @param set a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @param names a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(NamedDoubleSet set, String... names){
        String res = "[";
        if(names.length == 0){
            names = set.names().asArray();
        }
        boolean flag = true;
        for(String name: names){
            if(flag){
                flag = false;
            } else {
                res += ", ";
            }
            res += name + ":" + set.getValue(name);
        }
        return res + "]";
    }     
}

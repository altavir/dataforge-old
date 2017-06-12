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
package hep.dataforge.names;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.utils.MetaMorph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper class to store an ordered set of names
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Names extends Iterable<String>, MetaMorph {

    static Names of(String... names) {
        return new NameList(names);
    }

    static Names of(Iterable<String> names) {
        return new NameList(names);
    }

    static Names of(Names names) {
        return new NameList(names);
    }

    static Names of(Stream<String> names) {
        return new NameList(names.collect(Collectors.toList()));
    }

    static Names of(NameSetContainer set) {
        return new NameList(set.names());
    }

    /**
     * Порядок имеет значение!
     *
     * @return
     */
    default List<String> asList() {
        List<String> res = new ArrayList<>(size());
        for (String name : this) {
            res.add(name);
        }
        return res;
    }

    default String[] asArray() {
        return asList().toArray(new String[size()]);
    }

    String getName(int k);

    /**
     * Finds the number of the given name in list if numbering is supported
     *
     * @param str a {@link java.lang.String} object.
     * @return a int.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    int getNumberByName(String str) throws NameNotFoundException;

    /**
     * Checks if this Names contains all the names presented in the input array
     *
     * @param names
     * @return true only if all names a presented in this Names.
     */
    default boolean contains(String... names) {
        List<String> list = asList();
        boolean res = true;
        for (String name : names) {
            res = res && list.contains(name);
        }
        return res;
    }

    default boolean contains(Names names) {
        List<String> list = asList();
        boolean res = true;
        for (String name : names) {
            res = res && list.contains(name);
        }
        return res;
    }

    /**
     * @return
     */
    int size();

    default Names minus(String[] nuisancePars){
        return Names.of(asList().stream().filter(it-> !Arrays.sea))
    }
}

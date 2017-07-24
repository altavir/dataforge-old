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
package hep.dataforge.tables;

import hep.dataforge.names.Named;
import hep.dataforge.values.Value;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Column of values with format meta
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */

public interface Column extends Named, Iterable<Value> {

    ColumnFormat getFormat();

    @Override
    default String getName() {
        return getFormat().getName();
    }

    /**
     * Get the value with the given index
     * @param n
     * @return
     */
    Value get(int n);

    //TODO add custom value type accessors

    /**
     * Get values as list
     * @return
     */
    List<Value> asList();

    /**
     * The length of the column
     * @return
     */
    int size();

    /**
     * Get the values as a stream
     * @return
     */
    default Stream<Value> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}

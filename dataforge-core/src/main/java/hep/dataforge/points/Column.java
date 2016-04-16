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
package hep.dataforge.points;

import hep.dataforge.values.Value;
import hep.dataforge.values.ValueFormat;
import java.util.List;

/**
 * колонка однородных значений
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Column extends Iterable<Value> {
    /**
     * <p>format.</p>
     *
     * @return a {@link hep.dataforge.values.ValueFormat} object.
     */
    ValueFormat format();
    
    /**
     * <p>get.</p>
     *
     * @param n a int.
     * @return a {@link hep.dataforge.values.Value} object.
     */
    Value get(int n);
    
    /**
     * <p>asList.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<Value> asList();
    
}

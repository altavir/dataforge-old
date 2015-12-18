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
package hep.dataforge.data;

import hep.dataforge.values.Value;
import hep.dataforge.values.ValueFormat;
import hep.dataforge.values.ValueFormatFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple immutable Column implementation
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ListColumn implements Column {
    
    private final List<Value> values;
    private final ValueFormat format;

    /**
     * <p>Constructor for ListColumn.</p>
     *
     * @param values a {@link java.util.List} object.
     * @param format a {@link hep.dataforge.values.ValueFormat} object.
     */
    public ListColumn(List<Value> values, ValueFormat format) {
        this.values = values;
        this.format = format;
    }

    /**
     * <p>Constructor for ListColumn.</p>
     *
     * @param values a {@link java.util.List} object.
     */
    public ListColumn(List<Value> values) {
        this.values = values;
        format = ValueFormatFactory.EMPTY_FORMAT;
    }

    /**
     * {@inheritDoc}
     *
     * Возвращается копия листа, поэтому исходные данные остаются неизменными
     */
    @Override
    public List<Value> asList() {
        return new ArrayList<>(values);
    }
    
    /** {@inheritDoc} */
    @Override
    public ValueFormat format() {
        return format;
    }

    /** {@inheritDoc} */
    @Override
    public Value get(int n) {
        return values.get(n);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Value> iterator() {
        return values.iterator();
    }
    
}

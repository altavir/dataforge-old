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

import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueFormatFactory;
import hep.dataforge.values.ValueFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A simple immutable Column implementation using list of values
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ListColumn implements Column {
    
    private final Meta columnMeta;
    private final List<Value> values;
    private ValueFormatter format;

    public ListColumn(Meta meta, List<Value> values) {
        this.columnMeta = meta;
        this.values = values;
    }

    public ListColumn(List<Value> values) {
        columnMeta = Meta.buildEmpty("column");
        this.values = values;
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
    public ValueFormatter formatter() {
        if(format == null){
            format = ValueFormatFactory.build(columnMeta);
        }
            
        return format;
    }

    /** {@inheritDoc} */
    @Override
    public Value get(int n) {
        return values.get(n);
    }

    public Stream<Value> stream() {
        return values.stream();
    }

    @Override
    public Iterator<Value> iterator() {
        return values.iterator();
    }

    @Override
    public Meta meta() {
        return columnMeta;
    }

    @Override
    public int size() {
        return values.size();
    }
}

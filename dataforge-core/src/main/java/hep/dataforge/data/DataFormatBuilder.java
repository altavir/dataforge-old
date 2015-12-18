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

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.names.Names;
import hep.dataforge.values.ValueFormat;
import hep.dataforge.values.ValueFormatFactory;
import hep.dataforge.values.ValueType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * DataFormatBuilder class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DataFormatBuilder {

    private final List<String> nameList;
    private final Map<String, ValueFormat> formats;

    /**
     * <p>
     * Constructor for DataFormatBuilder.</p>
     */
    public DataFormatBuilder() {
        this.nameList = new ArrayList<>();
        this.formats = new HashMap<>();
    }

    public DataFormatBuilder(String... names) {
        this();
        for (String name : names) {
            addName(name);
        }
    }

    public DataFormatBuilder(Iterable<String> names) {
        this();
        for (String name : names) {
            addName(name);
        }
    }

    private void addName(String name) {
        if (!nameList.contains(name)) {
            nameList.add(name);
        } else {
            throw new NamingException("Dublicate name");
        }
    }

    /**
     * <p>
     * addString.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataFormatBuilder} object.
     */
    public DataFormatBuilder addString(String name) {
        addName(name);
        formats.put(name, ValueFormatFactory.forType(ValueType.STRING, name.length()));
        return this;
    }

    /**
     * <p>
     * addNumber.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataFormatBuilder} object.
     */
    public DataFormatBuilder addNumber(String name) {
        addName(name);
        formats.put(name, ValueFormatFactory.forType(ValueType.NUMBER, name.length()));
        return this;
    }

    /**
     * <p>
     * addTime.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataFormatBuilder} object.
     */
    public DataFormatBuilder addTime(String name) {
        addName(name);
        formats.put(name, ValueFormatFactory.forType(ValueType.TIME, name.length()));
        return this;
    }

    public DataFormatBuilder setFormat(String name, ValueFormat format) {
        if (!nameList.contains(name)) {
            addName(name);
        }
        this.formats.put(name, format);
        return this;
    }

    public DataFormatBuilder setFormat(String name, ValueType type) {
        return setFormat(name, ValueFormatFactory.forType(type));
    }

    public DataFormatBuilder setFormat(String name, ValueType type, int minSize) {
        return setFormat(name, ValueFormatFactory.forType(type, minSize));
    }

    /**
     * <p>
     * build.</p>
     *
     * @return a {@link hep.dataforge.data.DataFormat} object.
     */
    public DataFormat build() {
        return new DataFormat(Names.of(nameList), formats);
    }

}

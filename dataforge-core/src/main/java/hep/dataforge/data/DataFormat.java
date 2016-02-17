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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Names;
import hep.dataforge.values.ColumnFormat;
import hep.dataforge.values.FixedWidthFormat;
import hep.dataforge.values.ValueFormat;
import hep.dataforge.values.ValueFormatFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * DataFormat class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DataFormat implements Names {

    private final Names names;
    private final Map<String, ValueFormat> formats;

    public static DataFormat fromMeta(Meta annotation) {
        if (annotation.hasNode("column")) {
            Map<String, ValueFormat> map = new LinkedHashMap<>();
            for (Meta head : annotation.getNodes("column")) {
                map.put(head.getString("name"), ValueFormatFactory.build(head));
            }
            return new DataFormat(map);
        } else if (annotation.hasValue("names")) {
            return DataFormat.forNames(annotation.getStringArray("names"));
        } else {
            return new DataFormat();
        }
    }

    public static Meta toMeta(DataFormat format) {
        MetaBuilder builder = new MetaBuilder("format");
        for (String name : format) {
            MetaBuilder column = new MetaBuilder("column");
            column.putValue("name", name);
            if (format.formats.containsKey(name)) {
                ValueFormat vf = format.formats.get(name);
                if (vf instanceof ColumnFormat) {
                    ColumnFormat cf = (ColumnFormat) vf;
                    column.putValue("type", cf.primaryType().name());
                    if (vf instanceof FixedWidthFormat) {
                        column.putValue("width", cf.getMaxWidth());
                    }
                }
            }
            builder.putNode(column);
        }
        return builder.build();
    }

    /**
     * <p>
     * forPoint.</p>
     *
     * @param width
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataFormat} object.
     */
    public static DataFormat forNames(int width, String... names) {
        return forNames(width, Arrays.asList(names));
    }

    public static DataFormat forNames(String... names) {
        return forNames(Arrays.asList(names));
    }

    public static DataFormat forNames(Iterable<String> names) {
        return new DataFormat(Names.of(names));
    }

    public static DataFormat forNames(int width, Iterable<String> names) {
        Map<String, ValueFormat> formats = new LinkedHashMap<>();
        if (width > 0) {
            for (String name : names) {
                formats.put(name, ValueFormatFactory.fixedWidth(Math.max(width, name.length())));
            }
        }
        return new DataFormat(formats);
    }

    public static DataFormat forPoint(DataPoint point) {
        //TODO добавить тут возможность выбора подсписка?
        Names names = Names.of(point);
        Map<String, ValueFormat> map = new LinkedHashMap<>();
        for (String name : names) {
            map.put(name, ValueFormatFactory.forValue(point.getValue(name)));
        }
        return new DataFormat(names, map);
    }

    public DataFormat(Map<String, ValueFormat> formats) {
        names = Names.of(formats.keySet());
        this.formats = formats;
    }

    public DataFormat(Names names, Map<String, ValueFormat> formats) {
        this.names = names;
        this.formats = formats;
    }

    public DataFormat(Names names) {
        this.names = names;
        this.formats = Collections.emptyMap();
    }

//    /**
//     * Constructs format for given names with fixed with
//     *
//     * @param width
//     * @param names a {@link hep.dataforge.names.Names} object.
//     */
//    public DataFormat(int width, Names names) {
//        this.names = names;
//        formats = new LinkedHashMap<>();
//        if (width > 0) {
//            for (String name : names) {
//                formats.put(name, ValueFormatFactory.fixedWidth(Math.max(width, name.length())));
//            }
//        }
//    }
    /**
     * Free format
     */
    public DataFormat() {
        names = Names.of();
        formats = new LinkedHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimension() {
        return names.getDimension();
    }

    /**
     * {@inheritDoc}
     *
     * @param k
     * @return
     */
    @Override
    public String getName(int k) {
        return names.getName(k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberByName(String str) throws NameNotFoundException {
        return names.getNumberByName(str);
    }

    /**
     * <p>
     * getValueFormat.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.values.ValueFormat} object.
     */
    public ValueFormat getValueFormat(String name) {
        if (formats.containsKey(name)) {
            return formats.get(name);
        } else if (names.contains(name)) {
            return ValueFormatFactory.EMPTY_FORMAT;
        } else {
            throw new NameNotFoundException(name);
        }
    }

    /**
     * <p>
     * format.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a {@link java.lang.String} object.
     */
    public String format(DataPoint point) {
        return this
                .asList()
                .stream()
                .map((name) -> getValueFormat(name).format(point.getValue(name)))
                .collect(Collectors.joining("\t"));
    }

    /**
     * <p>
     * formatCaption.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String formatCaption() {
        return this
                .asList()
                .stream()
                .map((name) -> getValueFormat(name).formatString(name))
                .collect(Collectors.joining("\t"));

    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Iterator<String> iterator() {
        return names.iterator();
    }

    /**
     * <p>
     * subSet.</p>
     *
     * @param newNames a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataFormat} object.
     */
    public DataFormat subSet(String... newNames) {
        //Если список пустой, значит допустимы все имена
        if (this.names.asList().isEmpty()) {
            return DataFormat.forNames(0, newNames);
        }

        if (!this.names.contains(newNames)) {
            throw new IllegalArgumentException();
        }
        Map<String, ValueFormat> newFormat = new LinkedHashMap<>();
        for (String newName : newNames) {
            if (this.formats.containsKey(newName)) {
                newFormat.put(newName, formats.get(newName));
            }
        }
        return new DataFormat(Names.of(newNames), newFormat);
    }

    /**
     * <p>
     * isEmpty.</p>
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        return this.getDimension() == 0;
    }

}

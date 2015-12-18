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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.data.DataParser;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.ListDataSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * ColumnedDataReader class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ColumnedDataReader implements Iterable<DataPoint> {

    private DataPointStringIterator reader;
    private String meta;

    /**
     * <p>
     * Constructor for ColumnedDataReader.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param parser a {@link hep.dataforge.data.DataParser} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataReader(File file, DataParser parser) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        this.meta = readMeta(stream);
        this.reader = new DataPointStringIterator(stream, parser);
    }

    /**
     * <p>
     * Constructor for ColumnedDataReader.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param format an array of {@link java.lang.String} objects.
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataReader(File file, String[] format) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        this.meta = readMeta(stream);
        this.reader = new DataPointStringIterator(stream, format);
    }

    /**
     * <p>
     * Constructor for ColumnedDataReader.</p>
     *
     * @param file a {@link java.io.File} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataReader(File file) throws FileNotFoundException {
        Iterator<String> iterator = new LineIterator(file);
        if (!iterator.hasNext()) {
            throw new IllegalStateException();
        }
        String headline = iterator.next();
        this.reader = new DataPointStringIterator(iterator, headline);
    }

    private String readMeta(InputStream stream) {
        return "";
    }

    /**
     * <p>
     * skipLines.</p>
     *
     * @param n a int.
     */
    public void skipLines(int n) {
        reader.skip(n);
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public Iterator<DataPoint> iterator() {
        return reader;
    }

    /**
     * <p>Getter for the field <code>meta</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMeta() {
        return meta;
    }

    private Meta getAnnotation() {
        return MetaBuilder.buildEmpty("");
    }

    /**
     * <p>toDataSet.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.DataSet} object.
     */
    public DataSet toDataSet(String name) {
        Meta an = getAnnotation();
        List<DataPoint> points = new ArrayList<>();
        for (Iterator<DataPoint> iterator = this.iterator(); iterator.hasNext();) {
            points.add(iterator.next());
        }
        return new ListDataSet(name, an, points);
    }

}

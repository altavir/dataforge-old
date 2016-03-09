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

import hep.dataforge.points.DataPoint;
import hep.dataforge.points.SimpleParser;
import java.io.InputStream;
import java.util.Iterator;
import hep.dataforge.points.PointParser;

/**
 *
 * Считаем, что формат файла следующий: сначала идут метаданные, потом данные
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DataPointStringIterator implements Iterator<DataPoint> {

    private final Iterator<String> reader;
    private final PointParser parser;

    /**
     * <p>
     * Constructor for DataPointStringIterator.</p>
     *
     * @param reader a {@link java.util.Iterator} object.
     * @param parser a {@link hep.dataforge.points.PointParser} object.
     */
    public DataPointStringIterator(Iterator<String> reader, PointParser parser) {
        this.reader = reader;
        this.parser = parser;
    }

    /**
     * <p>
     * Constructor for DataPointStringIterator.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param parser a {@link hep.dataforge.points.PointParser} object.
     */
    public DataPointStringIterator(InputStream stream, PointParser parser) {
        this.reader = new LineIterator(stream);
        this.parser = parser;
    }

    /**
     * <p>
     * Constructor for DataPointStringIterator.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param names an array of {@link java.lang.String} objects.
     */
    public DataPointStringIterator(InputStream stream, String[] names) {
        this.reader = new LineIterator(stream);
        this.parser = new SimpleParser(names);
    }

    /**
     * <p>
     * Constructor for DataPointStringIterator.</p>
     *
     * @param reader a {@link java.util.Iterator} object.
     * @param names an array of {@link java.lang.String} objects.
     */
    public DataPointStringIterator(Iterator<String> reader, String[] names) {
        this.reader = reader;
        this.parser = new SimpleParser(names);
    }

    /**
     * <p>
     * Constructor for DataPointStringIterator.</p>
     *
     * @param reader a {@link java.util.Iterator} object.
     * @param headline a {@link java.lang.String} object.
     */
    public DataPointStringIterator(Iterator<String> reader, String headline) {
        this.reader = reader;
        this.parser = new SimpleParser(headline);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return reader.hasNext();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public DataPoint next() {
        DataPoint point = parser.parse(reader.next());
        return point;
    }

    /**
     * <p>
     * skip.</p>
     *
     * @param n a int.
     */
    public void skip(int n) {
        for (int i = 0; i < n; i++) {
            reader.next();
        }
    }

}

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

import hep.dataforge.tables.PointParser;
import hep.dataforge.tables.SimpleParser;
import hep.dataforge.values.Values;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * Считаем, что формат файла следующий: сначала идут метаданные, потом данные
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class DataPointStringIterator implements Iterator<Values> {

    private final Iterator<String> reader;
    private final PointParser parser;
    private volatile int pos = 0;

    public DataPointStringIterator(Iterator<String> reader, PointParser parser) {
        this.reader = reader;
        this.parser = parser;
    }

    public DataPointStringIterator(InputStream stream, PointParser parser) {
        this.reader = new LineIterator(stream);
        this.parser = parser;
    }

    public DataPointStringIterator(InputStream stream, String[] names) {
        this.reader = new LineIterator(stream);
        this.parser = new SimpleParser(names);
    }

    public DataPointStringIterator(Iterator<String> reader, String[] names) {
        this.reader = reader;
        this.parser = new SimpleParser(names);
    }

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
    public Values next() {
        String nextLine = reader.next();
        try {
            Values point = parser.parse(nextLine);
            return point;
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to read DataPoint at {} for line '{}'", pos, nextLine);
            return null;
        } finally {
            pos++;
        }
    }

    public void skip(int n) {
        for (int i = 0; i < n; i++) {
            reader.next();
            pos++;
        }
    }

    public int getPos() {
        return pos;
    }

}

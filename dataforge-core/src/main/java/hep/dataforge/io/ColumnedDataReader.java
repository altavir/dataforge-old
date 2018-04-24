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

import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.PointParser;
import hep.dataforge.tables.Table;
import hep.dataforge.values.Values;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ColumnedDataReader implements Iterable<Values> {

    private DataPointStringIterator reader;

    public ColumnedDataReader(InputStream stream, PointParser parser) {

        this.reader = new DataPointStringIterator(stream, parser);
    }

    public ColumnedDataReader(InputStream stream, String... format) {
        this.reader = new DataPointStringIterator(stream, format);
    }

    public ColumnedDataReader(Path path) throws IOException {
        String headline = Files.lines(path)
                .filter(line -> line.startsWith("#f") || (!line.isEmpty() && !line.startsWith("#")))
                .findFirst().get().substring(2);

        InputStream stream = Files.newInputStream(path);
        Iterator<String> iterator = new LineIterator(stream);
        if (!iterator.hasNext()) {
            throw new IllegalStateException();
        }
        this.reader = new DataPointStringIterator(iterator, headline);
    }

    public void skipLines(int n) {
        reader.skip(n);
    }

    @Override
    public Iterator<Values> iterator() {
        return reader;
    }

    public Table toTable() {
        List<Values> points = new ArrayList<>();
        for (Values p : this) {
            if (p != null) {
                points.add(p);
            }
        }
        return ListTable.Companion.infer(points);
    }

}

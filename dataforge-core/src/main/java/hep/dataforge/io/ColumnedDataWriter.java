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

import hep.dataforge.points.PointFormat;
import hep.dataforge.points.DataPoint;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Scanner;
import org.slf4j.LoggerFactory;
import hep.dataforge.points.PointSet;

/**
 * Вывод форматированного набора данных в файл или любой другой поток вывода
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ColumnedDataWriter implements AutoCloseable {

    private final PrintWriter writer;
    private final PointFormat format;

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param stream a {@link java.io.OutputStream} object.
     * @param names a {@link java.lang.String} object.
     */
    public ColumnedDataWriter(OutputStream stream, String... names) {
        this(stream, PointFormat.forNames(8, names));
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param stream a {@link java.io.OutputStream} object.
     * @param format a {@link hep.dataforge.points.PointFormat} object.
     */
    public ColumnedDataWriter(OutputStream stream, PointFormat format) {
        this.writer = new PrintWriter(stream);
        this.format = format;
    }

    public ColumnedDataWriter(OutputStream stream, Charset encoding, PointFormat format) {
        this.writer = new PrintWriter(new OutputStreamWriter(stream, encoding));
        this.format = format;
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param append a boolean.
     * @param names a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataWriter(File file, boolean append, String... names) throws FileNotFoundException {
        this(file, append, Charset.defaultCharset(), PointFormat.forNames(8, names));
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param append a boolean.
     * @param format a {@link hep.dataforge.points.PointFormat} object.
     * @param encoding
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataWriter(File file, boolean append, Charset encoding, PointFormat format) throws FileNotFoundException {
        this(new FileOutputStream(file, append), encoding, format);
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    public void close() throws Exception {
        this.writer.close();
    }

    /**
     * Добавить однострочный или многострочный комментарий
     *
     * @param str a {@link java.lang.String} object.
     */
    public void comment(String str) {
        Scanner sc = new Scanner(str);
        while (sc.hasNextLine()) {
            if (!str.startsWith("#")) {
                writer.print("#\t");
            }
            writer.println(sc.nextLine());
        }
    }

    /**
     * <p>
     * writePoint.</p>
     *
     * @param point a {@link hep.dataforge.points.DataPoint} object.
     */
    public void writePoint(DataPoint point) {
        writer.println(format.format(point));
        writer.flush();
    }

    /**
     * <p>
     * writePointList.</p>
     *
     * @param collection a {@link java.util.Collection} object.
     */
    public void writePointList(Collection<DataPoint> collection) {
        collection.stream().forEach((dp) -> {
            writePoint(dp);
        });
    }

    /**
     * <p>
     * writeHeader.</p>
     */
    public void writeFormatHeader() {
        writer.print("# ");
        writer.println(format.formatCaption());
        writer.flush();
    }

    /**
     * <p>
     * ln.</p>
     */
    public void ln() {
        writer.println();
    }

    /**
     * <p>
     * writeDataSet.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param data a {@link hep.dataforge.points.PointSet} object.
     * @param head a {@link java.lang.String} object.
     * @param append a boolean.
     * @param names a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static void writeDataSet(File file, PointSet data, String head, boolean append, String... names) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file, append)) {
            writeDataSet(os, data, head, names);
        }
    }

    public static void writeDataSet(OutputStream stream, PointSet data, String head, String... names) {
        ColumnedDataWriter writer;
        PointFormat format;
        if (data.getFormat().isEmpty()) {
            //Если набор задан в свободной форме, то конструируется автоматический формат по первой точке
            format = PointFormat.forPoint(data.get(0));
            LoggerFactory.getLogger(ColumnedDataWriter.class)
                    .debug("No DataSet format defined. Constucting default based on the first data point");
        } else {
            format = data.getFormat();
        }

        if (names.length == 0) {
            writer = new ColumnedDataWriter(stream, format);
        } else {
            writer = new ColumnedDataWriter(stream, format.subSet(names));
        }
        writer.comment(head);
        writer.ln();
//        if (!data.meta().isEmpty()) {
//            writer.comment(new XMLMetaWriter().writeString(data.meta(), null));
//        }
        writer.writeFormatHeader();
        for (DataPoint dp : data) {
            writer.writePoint(dp);
        }
        writer.ln();
    }

}

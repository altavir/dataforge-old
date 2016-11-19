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

import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointSource;
import hep.dataforge.tables.TableFormat;
import hep.dataforge.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Scanner;

/**
 * Вывод форматированного набора данных в файл или любой другой поток вывода
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ColumnedDataWriter implements AutoCloseable {

    private final PrintWriter writer;
    private final TableFormat format;

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param stream a {@link java.io.OutputStream} object.
     * @param names  a {@link java.lang.String} object.
     */
    public ColumnedDataWriter(OutputStream stream, String... names) {
        this(stream, TableFormat.fixedWidth(8, names));
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param stream a {@link java.io.OutputStream} object.
     * @param format a {@link hep.dataforge.tables.TableFormat} object.
     */
    public ColumnedDataWriter(OutputStream stream, TableFormat format) {
        this(stream, Utils.UTF, format);
    }

    public ColumnedDataWriter(OutputStream stream, Charset encoding, TableFormat format) {
        this.writer = new PrintWriter(new OutputStreamWriter(stream, encoding));
        this.format = format;
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param file   a {@link java.io.File} object.
     * @param append a boolean.
     * @param names  a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataWriter(File file, boolean append, String... names) throws FileNotFoundException {
        this(file, append, Charset.defaultCharset(), TableFormat.fixedWidth(8, names));
    }

    /**
     * <p>
     * Constructor for ColumnedDataWriter.</p>
     *
     * @param file     a {@link java.io.File} object.
     * @param append   a boolean.
     * @param format   a {@link hep.dataforge.tables.TableFormat} object.
     * @param encoding
     * @throws java.io.FileNotFoundException if any.
     */
    public ColumnedDataWriter(File file, boolean append, Charset encoding, TableFormat format) throws FileNotFoundException {
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
     * @param point a {@link hep.dataforge.tables.DataPoint} object.
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
        writer.println(IOUtils.formatCaption(format));
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
     * @param file   a {@link java.io.File} object.
     * @param data   a {@link hep.dataforge.tables.Table} object.
     * @param head   a {@link java.lang.String} object.
     * @param append a boolean.
     * @param names  a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static void writeDataSet(File file, PointSource data, String head, boolean append, String... names) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file, append)) {
            writeDataSet(os, data, head, names);
        }
    }

    public static void writeDataSet(OutputStream stream, PointSource data, String head, String... names) {
        ColumnedDataWriter writer;
        TableFormat format;
        if (data.getFormat().names().getDimension() == 0) {
            //Если набор задан в свободной форме, то конструируется автоматический формат по первой точке
            format = TableFormat.forPoint(data.iterator().next());
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

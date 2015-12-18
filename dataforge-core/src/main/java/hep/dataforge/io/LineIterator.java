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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

/**
 * Разбивает поток на строки и последовательно их считывает. Строки,
 * начинающиеся с '*' и пустые строки игнрорируются. Пробелы в начале и конце
 * строки игнорируются
 *
 * @author Alexander Nozik, based on commons-io code
 * @version $Id: $Id
 */
public class LineIterator implements Iterator<String>, AutoCloseable {

    private String commentStr = "#";

    private final BufferedReader reader;
    private String cachedLine;
    private boolean finished = false;

    /**
     * <p>
     * Constructor for LineIterator.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param charset a {@link java.lang.String} object.
     * @throws java.io.UnsupportedEncodingException if any.
     */
    public LineIterator(InputStream stream, String charset) throws UnsupportedEncodingException {
        this(new InputStreamReader(stream, charset));
    }

    /**
     * <p>
     * Constructor for LineIterator.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     */
    public LineIterator(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    /**
     * <p>
     * Constructor for LineIterator.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param charset a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.UnsupportedEncodingException if any.
     */
    public LineIterator(File file, String charset) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileInputStream(file), charset);
    }

    /**
     * <p>
     * Constructor for LineIterator.</p>
     *
     * @param file a {@link java.io.File} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public LineIterator(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    /**
     * <p>
     * Constructor for LineIterator.</p>
     *
     * @param reader a {@link java.io.Reader} object.
     */
    public LineIterator(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    /**
     * <p>
     * Getter for the field <code>commentStr</code>.</p>
     *
     * @return the commentStr
     */
    public String getCommentStr() {
        return commentStr;
    }

    /**
     * <p>
     * Setter for the field <code>commentStr</code>.</p>
     *
     * @param commentStr the commentStr to set
     */
    public void setCommentStr(String commentStr) {
        this.commentStr = commentStr;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        if (cachedLine != null) {
            return true;
        } else if (finished) {
            return false;
        } else {
            try {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line == null) {
                        finished = true;
                        return false;
                    } else if (isValidLine(line)) {
                        cachedLine = line.trim();
                        return true;
                    }
                }
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * <p>
     * isValidLine.</p>
     *
     * @param line a {@link java.lang.String} object.
     * @return a boolean.
     */
    protected boolean isValidLine(String line) {
        return !line.isEmpty() && !line.trim().startsWith(commentStr);
    }

    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public String next() {
        if (!hasNext()) {
            return null;
        }
        String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        finished = true;
        cachedLine = null;
    }

}

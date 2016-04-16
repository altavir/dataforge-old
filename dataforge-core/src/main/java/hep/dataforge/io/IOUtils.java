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
import hep.dataforge.points.ListPointSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.compile;

/**
 * <p>
 * IOUtils class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class IOUtils {

    /**
     * <p>
     * parse.</p>
     *
     * @param line a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] parse(String line) {
        Scanner scan = new Scanner(line);
        ArrayList<String> tokens = new ArrayList<>();
        String token;
        Pattern pat = compile("[\"\'].*[\"\']");
        while (scan.hasNext()) {
            if (scan.hasNext("[\"\'].*")) {
                token = scan.findInLine(pat);
                if (token != null) {
                    token = token.substring(1, token.length() - 1);
                } else {
                    throw new RuntimeException("Syntax error.");
                }
            } else {
                token = scan.next();
            }
            tokens.add(token);
        }
        return tokens.toArray(new String[tokens.size()]);

    }

//    public static ListPointSet readColumnedData(File input, String... names) {
//        return readColumnedData(null, input, names);
//    }
//
//    public static ListPointSet readColumnedData(String name, File input, String... names) throws FileNotFoundException {
//        ColumnedDataReader reader;
//        if (names.length > 0) {
//            reader = new ColumnedDataReader(input, names);
//        } else {
//            reader = new ColumnedDataReader(input);
//        }
//        ListPointSet res = new ListPointSet(name, names);
//        for (DataPoint dp : reader) {
//            res.add(dp);
//        }
//        return res;
//    }
    /**
     * <p>
     * readColumnedData.</p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.points.ListPointSet} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static ListPointSet readColumnedData(String fileName, String... names) throws FileNotFoundException {
        return readColumnedData(new File(fileName), names);
    }

    /**
     * <p>
     * readColumnedData.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.points.ListPointSet} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static ListPointSet readColumnedData(File file, String... names) throws FileNotFoundException {
        ColumnedDataReader reader;
        if (names.length == 0) {
            reader = new ColumnedDataReader(file);
        } else {
            reader = new ColumnedDataReader(file, names);
        }
        ListPointSet res = new ListPointSet(names);
        for (DataPoint dp : reader) {
            res.add(dp);
        }
        return res;
    }

    /**
     * <p>
     * readFileMask.</p>
     *
     * @param workDir a {@link java.io.File} object.
     * @param mask a {@link java.lang.String} object.
     * @return an array of {@link java.io.File} objects.
     */
    public static File[] readFileMask(File workDir, String mask) {
        File dir;
        String newMask;
        //отрываем инфомацию о директории
        if (mask.contains(File.separator)) {
            int k = mask.lastIndexOf(File.separatorChar);
            dir = new File(workDir, mask.substring(0, k));
            newMask = mask.substring(k + 1);
        } else {
            dir = workDir;
            newMask = mask;
        }

        String regex = newMask.toLowerCase().replace(".", "\\.").replace("?", ".?").replace("*", ".+");
        return dir.listFiles(new RegexFilter(regex));
    }

    /**
     * <p>getFile.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param path a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getFile(File file, String path) {
        File f = new File(path);

        if (f.isAbsolute()) {
            return f;
        }

        if (file.isDirectory()) {
            return new File(file, path);
        } else {
            return new File(file.getParentFile(), path);
        }
    }

    private static class RegexFilter implements FilenameFilter {

        String regex;

        public RegexFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().matches(regex);
        }

    }

}

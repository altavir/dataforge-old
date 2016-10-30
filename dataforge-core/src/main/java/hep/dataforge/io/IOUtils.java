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
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.TableFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * Constant <code>ANSI_RESET="\u001B[0m"</code>
     */
    public static final String ANSI_RESET = "\u001B[0m";
    /**
     * Constant <code>ANSI_BLACK="\u001B[30m"</code>
     */
    public static final String ANSI_BLACK = "\u001B[30m";
    /**
     * Constant <code>ANSI_RED="\u001B[31m"</code>
     */
    public static final String ANSI_RED = "\u001B[31m";
    /**
     * Constant <code>ANSI_GREEN="\u001B[32m"</code>
     */
    public static final String ANSI_GREEN = "\u001B[32m";
    /**
     * Constant <code>ANSI_YELLOW="\u001B[33m"</code>
     */
    public static final String ANSI_YELLOW = "\u001B[33m";
    /**
     * Constant <code>ANSI_BLUE="\u001B[34m"</code>
     */
    public static final String ANSI_BLUE = "\u001B[34m";
    /**
     * Constant <code>ANSI_PURPLE="\u001B[35m"</code>
     */
    public static final String ANSI_PURPLE = "\u001B[35m";
    /**
     * Constant <code>ANSI_CYAN="\u001B[36m"</code>
     */
    public static final String ANSI_CYAN = "\u001B[36m";
    /**
     * Constant <code>ANSI_WHITE="\u001B[37m"</code>
     */
    public static final String ANSI_WHITE = "\u001B[37m";


    public static String wrapANSI(String str, String ansiColor) {
        return ansiColor + str + ANSI_RESET;
    }

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

    public static Table readColumnedData(String fileName, String... names) throws FileNotFoundException {
        return readColumnedData(new File(fileName), names);
    }

    public static Table readColumnedData(File file, String... names) throws FileNotFoundException {
        return readColumnedData(new FileInputStream(file));
    }

    public static Table readColumnedData(InputStream stream, String... names) {
        ColumnedDataReader reader;
        if (names.length == 0) {
            reader = new ColumnedDataReader(stream);
        } else {
            reader = new ColumnedDataReader(stream, names);
        }
        ListTable.Builder res = new ListTable.Builder(names);
        for (DataPoint dp : reader) {
            res.row(dp);
        }
        return res.build();
    }

    public static String formatCaption(TableFormat format) {
        return "#f" + format.names()
                .asList()
                .stream()
                .map((name) -> format.getValueFormat(name).formatString(format.getTitle(name)))
                .collect(Collectors.joining("\t"));
    }

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

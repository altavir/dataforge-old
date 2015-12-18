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

import hep.dataforge.content.AbstractContent;
import hep.dataforge.meta.Meta;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import static hep.dataforge.io.IOUtils.readFileMask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Связь с файлом данных, оформленная в виде контента
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FileData extends AbstractContent implements BinaryData {

    /** Constant <code>FILE_META="meta"</code> */
    public static final String FILE_META = "meta";

    /**
     * <p>
     * buildFromAnnotation.</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param an a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.data.FileData} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static FileData build(File parent, Meta an) throws FileNotFoundException {
        File file = new File(parent, getPath(an));
        FileData res;
        if (an.hasValue("name")) {
            res = new FileData(an.getString("name"), file);
        } else {
            res = new FileData(file);
        }
        res.configure(an.getNode(FILE_META, an));
        return res;
    }

    /**
     * <p>
     * getFileDataSourcesByMask.</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param an a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link java.util.List} object.
     * @throws java.io.FileNotFoundException if any.
     */
    @NodeDef(name = "meta", info = "Any metadata for this file or list of files")
    @ValueDef(name = "path", info = "Path to the file or file mask")
    public static List<FileData> buildByMask(File parent, Meta an) throws FileNotFoundException {
        //FIXME переделать механизм импорта
        String path = getPath(an);

        List<FileData> res = new ArrayList<>();
        File[] maskfiles = readFileMask(parent, path);
        for (File file : maskfiles) {
            if (file.exists() && (!file.isDirectory())) {
                FileData fds = new FileData(file);
                fds.configure(an.getNode(FILE_META, an));
                res.add(fds);
            }
        }
        return res;
    }

    /**
     * <p>
     * getFileDataSourcesByMask.</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param mask a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public static List<FileData> buildFromString(File parent, String mask) throws FileNotFoundException {
        List<FileData> res = new ArrayList<>();
        File[] maskfiles = readFileMask(parent, mask);
        if(maskfiles.length == 0){
            LoggerFactory.getLogger(FileData.class).warn("The mask '{}' has no matches.", mask);
        }
        for (File file : maskfiles) {
            if (file.exists() && (!file.isDirectory())) {
                FileData fds = new FileData(file);
                res.add(fds);
            }
        }
        return res;
    }

    private static String getPath(Meta an) {
        if (an.hasValue("path")) {
            return an.getString("path");
        } else {
            return an.getString(an.getName());
        }
    }

    private File inputFile;
    private final String inputFileType;

    /**
     * <p>
     * Constructor for FileData.</p>
     *
     * @param parent a {@link java.io.File} object.
     * @param path a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public FileData(File parent, String path) throws FileNotFoundException {
        this(new File(parent, path));
    }

    /**
     * <p>
     * Constructor for FileData.</p>
     *
     * @param input a {@link java.io.File} object.
     * @throws java.io.FileNotFoundException if any.
     */
    public FileData(File input) throws FileNotFoundException {
        this(input.getName().replaceFirst("[.][^.]+$", ""), input);
    }

    private FileData(String name, File input) throws FileNotFoundException {
        super(name);
        inputFile = input;
        inputFileType = getFileExtension(inputFile);
        if (!input.exists() || !input.isFile()) {
            throw new FileNotFoundException();
        }
    }

    /**
     * <p>
     * getExtension.</p>
     *
     * @return the inputFileType
     */
    public String getExtension() {
        return inputFileType;
    }

    private String getFileExtension(File file) {
        String extension = "";
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension.toLowerCase();
    }

    /**
     * <p>
     * Getter for the field <code>inputFile</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getInputFile() {
        return inputFile;
    }

    /** {@inheritDoc}
     * @throws java.io.FileNotFoundException */
    @Override
    public FileInputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(inputFile);
    }

}

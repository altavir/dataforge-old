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

import hep.dataforge.actions.ActionResult;
import hep.dataforge.actions.Pack;
import hep.dataforge.content.ContentList;
import hep.dataforge.content.NamedGroup;
import hep.dataforge.context.Context;
import static hep.dataforge.data.FileData.FILE_META;
import hep.dataforge.dependencies.GenericDependency;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import hep.dataforge.context.Encapsulated;

/**
 * A replacement for obsolete ImportDataAction
 *
 * @author Alexander Nozik
 */
public class DataManager implements Encapsulated {

    /**
     * Constant <code>DATA_DIR="dataDir"</code>
     */
    public static final String DATA_DIR = "dataDir";

    /**
     * Constant <code>DATA_ELEMENT="data"</code>
     */
    public static final String DATA_ELEMENT = "data";

    /**
     * Constant <code>GROUP_TAG="dataGroup"</code>
     */
    public static final String GROUP_TAG = "dataGroup";
    private final Context context;

    public DataManager(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * <p>
     * read.</p>
     *
     * @param an
     * @return a {@link hep.dataforge.content.NamedGroup} object.
     * @throws java.io.IOException if any.
     */
    @ValueDef(name = "dataDir", info = "The absolute or relative path to the data directory. By default is taken from Context.")
    @NodeDef(name = "file", multiple = true, info = "Single file or multiple files imported by mask with the same metadata", target = "method::hep.dataforge.data.FileData.buildByMask")
    @NodeDef(name = "group", multiple = true, info = "The group of data with same metadata. The metadata is provided by 'meta' element. It is possible to combine multiple subgroups (in this case metatadata is merged).")
    public NamedGroup<FileData> read(Meta an) throws IOException, ContentException, ParseException {
        File parentDir = getContext().io().getRootDirectory();

        if (an.hasValue(DATA_DIR) || getContext().hasValue(DATA_DIR)) {
            parentDir = getContext().io().getFile(an.getString(DATA_DIR, getContext().getString(DATA_DIR)));
        }

        return new ContentList<>("data", readDataConfiguration(parentDir, an));
    }

    private List<FileData> readDataConfiguration(File parentDir, Meta ds) throws IOException, ContentException, ParseException {
        assert parentDir.exists() && parentDir.isDirectory();
        List<FileData> res = new ArrayList<>();

        if (ds.hasNode("file")) {
            List<? extends Meta> fileMaskAnnotations = ds.getNodes("file");
            for (Meta sourceCfg : fileMaskAnnotations) {
                res.addAll(FileData.buildByMask(parentDir, sourceCfg));
            }
        }

        if (ds.hasValue("file")) {
            List<Value> fileMasks = ds.getValue("file").listValue();
            for (Value val : fileMasks) {
                res.addAll(FileData.buildFromString(parentDir, val.stringValue()));
            }
        }

        //набор данных с одинаковыми параметрами
        if (ds.hasNode("group")) {
            List<? extends Meta> groupAnnotations = ds.getNodes("group");
            for (Meta an : groupAnnotations) {
                List<FileData> list = readDataConfiguration(parentDir, an);
                for (FileData d : list) {
                    // Обновляем аннотацию файла. Основной считаем аннотацию файла, а не контейнера

                    //FIXME сделать тут добавление группового тэга во всевложенные аннотации
                    if (an.hasNode(FILE_META)) {
                        Meta fileMeta = an.getNode(FILE_META);
                        d.setMeta(MergeRule.replace(d.meta(), fileMeta));
                    }
                    res.add(d);
                }
            }

        }

        if (ds.hasNode(SourceSetLoader.SOURCE_SET)) {
            for (Meta an : ds.getNodes(SourceSetLoader.SOURCE_SET)) {
                String path = an.getString("path", "sourcesets.xml");
                String tag = an.getString("tag");
                SourceSetLoader loader;
                try {
                    loader = SourceSetLoader.loadFromFile(getContext(), path);
                } catch (InterruptedException ex) {
                    throw new ContentException("Can't load data from sourse set file", ex);
                }
                Object sourceSet = loader.getSourceSet(tag);
                if (sourceSet instanceof NamedGroup) {
                    res.addAll(((NamedGroup) sourceSet).asList());
                } else {
                    res.add((FileData) sourceSet);
                }

            }
        }

        return res;
    }

    public ActionResult readFromConfig(Meta dataConfiguration) throws ContentException {
        if (dataConfiguration.hasNode(DATA_ELEMENT)) {
            dataConfiguration = dataConfiguration.getNode(DATA_ELEMENT);
        }

        File parentDir = getContext().io().getRootDirectory();

        if (dataConfiguration.hasValue(DATA_DIR) || getContext().hasValue(DATA_DIR)) {
            parentDir = getContext().io().getFile(dataConfiguration.getString(DATA_DIR, getContext().getString(DATA_DIR)));
        }

        try {
            List<FileData> res = readDataConfiguration(parentDir, dataConfiguration);
            res.stream().forEach((d) -> {
                getContext().log("Importing file {}", d.getInputFile().getName());
            });
            return new Pack(null, dataConfiguration, getContext(), FileData.class,
                    res.stream()
                    .map((FileData f) -> new GenericDependency.Builder<>(f).build(f.getName()))
                    .collect(Collectors.toList()));
        } catch (IOException | ParseException ex) {
            throw new ContentException("File not found");
        }
    }

}

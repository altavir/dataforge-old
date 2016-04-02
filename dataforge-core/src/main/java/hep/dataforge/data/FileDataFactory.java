/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.data.binary.Binary;
import hep.dataforge.data.binary.FileBinary;
import hep.dataforge.description.NodeDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@NodeDef(name = "file", info = "File data element or list of files with the same meta defined by mask.")
@NodeDef(name = "dir", info = "Directory data node.")
public class FileDataFactory extends DataFactory {

    public static final String FILE_NODE = "file";
    public static final String FILE_MASK_NODE = "files";
    public static final String DIRECTORY_NODE = "dir";
    public static final String DATA_DIR_KEY = "dataDir";

    public static final String FILE_NAME_KEY = "fileName";
    public static final String FILE_PATH_KEY = "filePath";

    @Override
    protected void buildChildren(Context context, DataTree.Builder<?> builder, Meta dataConfig) {
        File parentFile;
        if (dataConfig.hasNode(DATA_DIR_KEY)) {
            parentFile = context.io().getFile(dataConfig.getString(DATA_DIR_KEY));
        } else if (context.hasValue(DATA_DIR_KEY)) {
            parentFile = context.io().getFile(context.getString(DATA_DIR_KEY));
        } else {
            parentFile = context.io().getRootDirectory();
        }

        if (dataConfig.hasNode(FILE_NODE)) {
            dataConfig.getNodes(FILE_NODE).forEach((node) -> addFile(context, builder, parentFile, node));
        }

        if (dataConfig.hasNode(DIRECTORY_NODE)) {
            dataConfig.getNodes(DIRECTORY_NODE).forEach((node) -> addDir(context, builder, parentFile, node));
        }

        if (dataConfig.hasValue("file")) {
            Value fileValue = dataConfig.getValue("file");
            fileValue.listValue().stream().forEach((fileName) -> {
                addFile(context, builder, parentFile, new MetaBuilder("file")
                        .putValue("path", fileName));
            });
        }
    }

    private Data<Binary> buildFileData(File file, Meta meta) {
        StaticData<Binary> fileData = new StaticData(new FileBinary(file));
        MetaBuilder mb = new MetaBuilder(meta);
        mb.putValue(FILE_PATH_KEY, file.getAbsolutePath());
        mb.putValue(FILE_NAME_KEY, file.getName());
        fileData.setMeta(mb.build());
        return fileData;
    }

    /**
     *
     * @param context
     * @param builder
     * @param parentFile
     * @param fileNode
     */
    private void addFile(Context context, DataTree.Builder builder, File parentFile, Meta fileNode) {
        List<File> files = listFiles(context, parentFile, fileNode);
        if (files.isEmpty()) {
            context.getLogger().warn("No files matching the filter: " + fileNode.toString());
        } else if (files.size() == 1) {
            File file = files.get(0);
            Meta fileMeta = fileNode.hasNode(DATA_META_KEY) ? fileNode.getNode(DATA_META_KEY) : Meta.empty();
            builder.putData(fileName(file), buildFileData(file, fileMeta));
        } else {
            files.forEach(file -> {
                Meta fileMeta = fileNode.hasNode(DATA_META_KEY) ? fileNode.getNode(DATA_META_KEY) : Meta.empty();
                builder.putData(fileName(file), buildFileData(file, fileMeta));
            });
        }
    }

    private String fileName(File file) {
        return file.getName();
    }

    private List<File> listFiles(Context context, File parentFile, Meta fileNode) {
        String path = fileNode.getString("path");
        return Arrays.asList(parentFile.listFiles((File dir, String name)
                -> dir.equals(parentFile) && name.matches(path.replace("?", ".?").replace("*", ".*?"))));

    }

    private void addDir(Context context, final DataTree.Builder builder, File parentFile, Meta dirNode) {
        DataTree.Builder dirBuilder = DataTree.builder(File.class);
        File dir = new File(parentFile, dirNode.getString("path"));
        dirBuilder.setName(dirNode.getString(NODE_NAME_KEY, dirNode.getName()));
        if (dirNode.hasNode(DATA_META_KEY)) {
            dirBuilder.setMeta(dirNode.getNode(DATA_META_KEY));
        }

        boolean recurse = dirNode.getBoolean("recursive", true);

        for (File file : dir.listFiles()) {
            //TODO add file filter here
            if (file.isFile()) {
                dirBuilder.putData(fileName(file), buildFileData(file, Meta.empty()));
            } else if (recurse) {
                addDir(context, dirBuilder, dir, Meta.empty());
            }
        }
        builder.putBranch(dirBuilder.build());

    }

//        public static final String DATA_DIR = "dataDir";
//    public static final String DATA_ELEMENT = "data";
//    public static final String GROUP_TAG = "dataGroup";
//    public static final String FILE_META = "meta";
//    @ValueDef(name = "dataDir", info = "The absolute or relative path to the data directory. By default is taken from Context.")
//    @NodeDef(name = "file", multiple = true, info = "Single file or multiple files imported by mask with the same metadata", target = "method::hep.dataforge.data.FileData.buildByMask")
//    @NodeDef(name = "group", multiple = true, info = "The group of data with same metadata. The metadata is provided by 'meta' element. It is possible to combine multiple subgroups (in this case metatadata is merged).")
//    public DataNode<FileData> read(Meta an) throws IOException, ContentException, ParseException {
//        File parentDir = getContext().io().getRootDirectory();
//
//        if (an.hasValue(DATA_DIR) || getContext().hasValue(DATA_DIR)) {
//            parentDir = getContext().io().getFile(an.getString(DATA_DIR, getContext().getString(DATA_DIR)));
//        }
//
//        return new ContentList<>("data", readDataConfiguration(parentDir, an));
//    }
//
//    private DataNode readDataConfiguration(File parentDir, Meta ds) throws IOException, ContentException, ParseException {
//        assert parentDir.exists() && parentDir.isDirectory();
//        List<Data> res = new ArrayList<>();
//
//        if (ds.hasNode("file")) {
//            List<? extends Meta> fileMaskAnnotations = ds.getNodes("file");
//            for (Meta sourceCfg : fileMaskAnnotations) {
//                res.addAll(FileData.buildByMask(parentDir, sourceCfg));
//            }
//        }
//
//        if (ds.hasValue("file")) {
//            List<Value> fileMasks = ds.getValue("file").listValue();
//            for (Value val : fileMasks) {
//                res.addAll(FileData.buildFromString(parentDir, val.stringValue()));
//            }
//        }
//
//        //набор данных с одинаковыми параметрами
//        if (ds.hasNode("group")) {
//            List<? extends Meta> groupAnnotations = ds.getNodes("group");
//            for (Meta an : groupAnnotations) {
//                List<FileData> list = readDataConfiguration(parentDir, an);
//                list.stream().<Data>map((d) -> {
//                    // Обновляем аннотацию файла. Основной считаем аннотацию файла, а не контейнера
//
//                    //FIXME сделать тут добавление группового тэга во всевложенные аннотации
//                    if (an.hasNode(FILE_META)) {
//                        Meta fileMeta = an.getNode(FILE_META);
//                        d.setMeta(MergeRule.replace(d.meta(), fileMeta));
//                    }
//                    return d;
//                }).forEach((d) -> {
//                    res.add(d);
//                });
//            }
//
//        }
//
//        if (ds.hasNode(SourceSetLoader.SOURCE_SET)) {
//            for (Meta an : ds.getNodes(SourceSetLoader.SOURCE_SET)) {
//                String path = an.getString("path", "sourcesets.xml");
//                String tag = an.getString("tag");
//                SourceSetLoader loader;
//                try {
//                    loader = SourceSetLoader.loadFromFile(getContext(), path);
//                } catch (InterruptedException ex) {
//                    throw new ContentException("Can't load data from sourse set file", ex);
//                }
//                Object sourceSet = loader.getSourceSet(tag);
//                if (sourceSet instanceof NamedGroup) {
//                    res.addAll(((NamedGroup) sourceSet).asList());
//                } else {
//                    res.add((FileData) sourceSet);
//                }
//
//            }
//        }
//
//        return res;
//    }
//
//    public DataNode readFromConfig(Meta dataConfiguration) throws ContentException {
//        if (dataConfiguration.hasNode(DATA_ELEMENT)) {
//            dataConfiguration = dataConfiguration.getNode(DATA_ELEMENT);
//        }
//
//        File parentDir = getContext().io().getRootDirectory();
//
//        if (dataConfiguration.hasValue(DATA_DIR) || getContext().hasValue(DATA_DIR)) {
//            parentDir = getContext().io().getFile(dataConfiguration.getString(DATA_DIR, getContext().getString(DATA_DIR)));
//        }
//
//        try {
//            
//            //FIXME revise data holders
//            List<FileData> res = readDataConfiguration(parentDir, dataConfiguration);
//            res.stream().forEach((d) -> {
//                getContext().log("Importing file {}", d.getInputFile().getName());
//            });
//            return DataSet.builder(FileData.class)
//                    .putAll(res)
//                    .build();
//
//        } catch (IOException | ParseException ex) {
//            throw new ContentException("File not found");
//        }
//    }    
}

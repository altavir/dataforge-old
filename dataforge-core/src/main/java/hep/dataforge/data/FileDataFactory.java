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
public class FileDataFactory extends DataFactory<Binary> {

    public static final String FILE_NODE = "file";
    public static final String FILE_MASK_NODE = "files";
    public static final String DIRECTORY_NODE = "dir";
    public static final String DATA_DIR_KEY = "dataDir";

    public static final String FILE_NAME_KEY = "fileName";
    public static final String FILE_PATH_KEY = "filePath";

    public FileDataFactory() {
        super(Binary.class);
    }
    
    

    @Override
    protected void buildChildren(Context context, DataTree.Builder<Binary> builder, DataFilter filter, Meta dataConfig) {
        //FIXME add filtering here
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

        if (dataConfig.hasValue(FILE_NODE)) {
            Value fileValue = dataConfig.getValue(FILE_NODE);
            fileValue.listValue().stream().forEach((fileName) -> {
                addFile(context, builder, parentFile, new MetaBuilder(FILE_NODE)
                        .putValue("path", fileName));
            });
        }
    }
    
    public static Data<Binary> buildFileData(Context context, String filePath, Meta meta){
        return buildFileData(context.io().getFile(filePath), meta);
    }

    private static Data<Binary> buildFileData(File file, Meta meta) {
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
    private void addFile(Context context, DataTree.Builder<Binary> builder, File parentFile, Meta fileNode) {
        List<File> files = listFiles(context, parentFile, fileNode);
        if (files.isEmpty()) {
            context.getLogger().warn("No files matching the filter: " + fileNode.toString());
        } else if (files.size() == 1) {
            File file = files.get(0);
            Meta fileMeta = fileNode.hasNode(NODE_META_KEY) ? fileNode.getNode(NODE_META_KEY) : Meta.empty();
            builder.putData(fileName(file), buildFileData(file, fileMeta));
        } else {
            files.forEach(file -> {
                Meta fileMeta = fileNode.hasNode(NODE_META_KEY) ? fileNode.getNode(NODE_META_KEY) : Meta.empty();
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

    private void addDir(Context context, final DataTree.Builder<Binary> builder, File parentFile, Meta dirNode) {
        DataTree.Builder<Binary> dirBuilder = DataTree.builder(Binary.class);
        File dir = new File(parentFile, dirNode.getString("path"));
        dirBuilder.setName(dirNode.getString(NODE_NAME_KEY, dirNode.getName()));
        if (dirNode.hasNode(NODE_META_KEY)) {
            dirBuilder.setMeta(dirNode.getNode(NODE_META_KEY));
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
        builder.putNode(dirBuilder.build());

    }

}

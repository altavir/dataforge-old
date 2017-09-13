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
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static hep.dataforge.utils.NamingUtils.wildcardMatch;

@NodeDef(name = "file", info = "File data element or list of files with the same meta defined by mask.")
@NodeDef(name = "dir", info = "Directory data node.")
public class FileDataFactory extends DataFactory<Binary> {

    public static final String FILE_NODE = "file";
    public static final String FILE_MASK_NODE = "files";
    public static final String DIRECTORY_NODE = "dir";
    public static final String DATA_DIR_KEY = "dataDir";

    public static final String FILE_NAME_KEY = "fileName";
    public static final String FILE_PATH_KEY = "filePath";

    public static final String META_DIRECTORY = "@meta";

    public FileDataFactory() {
        super(Binary.class);
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    protected void fill(DataTree.Builder<Binary> builder, Context context, Meta dataConfig) {
        Path parentFile;
        if (dataConfig.hasMeta(DATA_DIR_KEY)) {
            parentFile = context.io().getFile(dataConfig.getString(DATA_DIR_KEY)).toPath();
        } else if (context.hasValue(DATA_DIR_KEY)) {
            parentFile = context.io().getFile(context.getString(DATA_DIR_KEY)).toPath();
        } else {
            parentFile = context.io().getRootDirectory().toPath();
        }

        /**
         * Add items matching specific file name. Not necessary one.
         */
        if (dataConfig.hasMeta(FILE_NODE)) {
            dataConfig.getMetaList(FILE_NODE).forEach((node) -> addFile(context, builder, parentFile, node));
        }

        /**
         * Add content of the directory
         */
        if (dataConfig.hasMeta(DIRECTORY_NODE)) {
            dataConfig.getMetaList(DIRECTORY_NODE).forEach((node) -> addDir(context, builder, parentFile, node));
        }

        if (dataConfig.hasValue(FILE_NODE)) {
            Value fileValue = dataConfig.getValue(FILE_NODE);
            fileValue.listValue().forEach((fileName) -> {
                addFile(context, builder, parentFile, new MetaBuilder(FILE_NODE)
                        .putValue("path", fileName));
            });
        }
    }

    public Data<Binary> buildFileData(Context context, String filePath, Meta meta) {
        return buildFileData(context.io().getFile(filePath).toPath(), meta);
    }

    private Data<Binary> buildFileData(Path file, Meta meta) {
        MetaBuilder mb = new MetaBuilder(meta);
        mb.putValue(FILE_PATH_KEY, file.toAbsolutePath().toString());
        mb.putValue(FILE_NAME_KEY, fileName(file));

        Meta fileMeta;
        if (meta.hasValue("metaFile")) {
            Path metaFile = file.resolveSibling(meta.getString("metaFile"));
            mb.removeValue("metaFile");
            try {
                Meta base = MetaFileReader.read(metaFile);
                fileMeta = new Laminate(meta, base);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            Path metaFile = Files.list(file.getParent().resolve(META_DIRECTORY)).filter(it-> it.startsWith(file.getFileName().toString())).findFirst();
            fileMeta = mb.build();
        }

        return Data.buildStatic(new FileBinary(file), fileMeta);
    }

    /**
     * @param context
     * @param builder
     * @param parentFile
     * @param fileNode
     */
    private void addFile(Context context, DataTree.Builder<Binary> builder, Path parentFile, Meta fileNode) {
        List<Path> files = listFiles(context, parentFile, fileNode);
        if (files.isEmpty()) {
            context.getLogger().warn("No files matching the filter: " + fileNode.toString());
        } else if (files.size() == 1) {
            Path file = files.get(0);
            Meta fileMeta = fileNode.getMetaOrEmpty(NODE_META_KEY);
            builder.putData(fileName(file), buildFileData(file, fileMeta));
        } else {
            files.forEach(file -> {
                Meta fileMeta = fileNode.getMetaOrEmpty(NODE_META_KEY);
                builder.putData(fileName(file), buildFileData(file, fileMeta));
            });
        }
    }

    private String fileName(Path file) {
        return file.getFileName().toString();
    }

    private List<Path> listFiles(Context context, Path parentFile, Meta fileNode) {
        String mask = fileNode.getString("path");
        try {
            return Files.list(parentFile).filter(path -> wildcardMatch(mask, path.toString())).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ValueDef(name = "path", info = "The relative path of directory to be added")
    @ValueDef(name = "recursive", type = ValueType.BOOLEAN, def = "true", info = "Flag to add subnodes recursively")
    private void addDir(Context context, final DataTree.Builder<Binary> builder, Path parentFile, Meta dirNode) {
        DataTree.Builder<Binary> dirBuilder = DataTree.builder(Binary.class);
        Path dir = parentFile.resolve(dirNode.getString("path"));
        if (!Files.isDirectory(dir)) {
            throw new RuntimeException("The directory " + dir + " does not exist");
        }
        dirBuilder.setName(dirNode.getString(NODE_NAME_KEY, dirNode.getName()));
        if (dirNode.hasMeta(NODE_META_KEY)) {
            dirBuilder.setMeta(dirNode.getMeta(NODE_META_KEY));
        }

        boolean recurse = dirNode.getBoolean("recursive", true);

        try {
            Files.list(dir).forEach(file -> {
                if (Files.isRegularFile(file)) {
                    dirBuilder.putData(fileName(file), buildFileData(file, Meta.empty()));
                } else if (recurse && !dir.getFileName().toString().equals(META_DIRECTORY)) {
                    addDir(context, dirBuilder, dir, Meta.empty());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        builder.putNode(dirBuilder.build());

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.StorageType;

/**
 * @author Alexander Nozik
 */
public class FileStorageFactory implements StorageType {

//    /**
//     * Create root file storage in the given local directory. Annotation is
//     * optional.
//     *
//     * @param directory
//     * @param def
//     * @return
//     * @throws StorageException
//     */
//    public static FileStorage in(File directory, Meta def) throws StorageException {
//        try {
//            FileObject localRoot = VFSUtils.getLocalFile(directory);
//            return in(localRoot, def);
//
//        } catch (FileSystemException ex) {
//            throw new StorageException(ex);
//        }
//    }
//
//    public static FileStorage in(FileObject remoteDir, Meta def) throws StorageException {
//        FileStorage res = new FileStorage(remoteDir, def);
////        res.loadConfig(def);
//        res.updateDirectoryLoaders();
//        return res;
//    }
//
//    /**
//     * Open existing storage in read only or read/write mode.
//     *
//     * @param remoteDir
//     * @param readOnly
//     * @return
//     * @throws StorageException
//     */
//    public static FileStorage connect(FileObject remoteDir, boolean readOnly, boolean monitor) throws StorageException {
//        try {
//            if (!remoteDir.exists() || !remoteDir.getType().equals(FOLDER)) {
//                throw new StorageException("Can't open storage. Target should be existing directory.");
//            }
//        } catch (FileSystemException ex) {
//            throw new StorageException("Can't open storage.");
//        }
//
//        Meta meta = new MetaBuilder("storage")
//                .setValue("path", remoteDir.getName().getURI())
//                .setValue("type", "file")
//                .setValue("readOnly", readOnly)
//                .setValue("monitor", monitor)
//                .build();
//
//        FileStorage res = new FileStorage(Global.instance(), meta);
//        res.refresh();
//        return res;
//    }
//
//    public static FileStorage connect(File directory, boolean readOnly, boolean monitor) throws StorageException {
//        try {
//            FileObject localRoot = VFSUtils.getLocalFile(directory);
//            return connect(localRoot, readOnly, monitor);
//
//        } catch (FileSystemException ex) {
//            throw new StorageException(ex);
//        }
//    }

    public static MetaBuilder buildStorageMeta(String path, boolean readOnly, boolean monitor){
        return new MetaBuilder("storage")
                .setValue("path", path)
                .setValue("type", "file")
                .setValue("readOnly", readOnly)
                .setValue("monitor", monitor);
    }

    @Override
    public String type() {
        return "file";
    }

    @Override
    public Storage build(Context context, Meta annotation) {
        return new FileStorage(context, annotation);
    }

}

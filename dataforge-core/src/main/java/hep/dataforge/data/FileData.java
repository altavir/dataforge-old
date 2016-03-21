/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.names.BaseMetaHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * A data container for Java File
 * @author Alexander Nozik
 */
public class FileData extends BaseMetaHolder implements StreamData {
    
    private final File file;

    public FileData(File file) {
        this.file = file;
    }

    @Override
    public CompletableFuture<InputStream> getInFuture() {
        try {
            return CompletableFuture.completedFuture(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            //FIXME replace by exceptional future
            throw new RuntimeException("Data evaluation failed: file not found", ex);
        }
    }

    @Override
    public Class<? super InputStream> dataType() {
        return InputStream.class;
    }

    public File getFile() {
        return file;
    }
    
    public String fileName(){
        return file.getName();
    }
}

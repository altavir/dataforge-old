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
package hep.dataforge.storage.commons;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.EnvelopeTypeLibrary;
import hep.dataforge.io.envelopes.MetaReaderLibrary;
import hep.dataforge.io.envelopes.MetaWriterLibrary;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import static hep.dataforge.storage.commons.EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE;
import hep.dataforge.storage.filestorage.FileStorage;
import hep.dataforge.storage.filestorage.FileStorageEnvelopeType;
import hep.dataforge.utils.MetaFactory;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author darksnake
 */
@PluginDef(name = "storage", group = "hep.dataforge", description = "Basic DataForge storage plugin")
public class StoragePlugin extends BasicPlugin {
    
    public static final String DEFAULT_STORAGE_TYPE = "file";

    /**
     * The map of available storage factories
     */
    private final Map<String, MetaFactory<Storage>> storageMap;
    
    private Context context;

    public StoragePlugin() {
        storageMap = new HashMap<>();
        storageMap.put("file", FileStorage::from);
    }
  
    @Override
    public void apply(Context context) {
        this.context = context;
        MetaReaderLibrary.instance().putComposite(1, "JSON", new JSONMetaReader());
        MetaWriterLibrary.instance().putComposite(1, "JSON", new JSONMetaWriter());
        EnvelopeTypeLibrary.instance().putComposite(DATAFORGE_STORAGE_ENVELOPE_CODE, "storage", new FileStorageEnvelopeType());
    }

    @Override
    public void clean(Context context) {
        this.context = null;
    }
    
    public void addStorageFactory(String type, MetaFactory<Storage> factory){
        this.storageMap.put(type, factory);
    } 
    
    public MetaFactory<Storage> getStorageFactory(String type){
        if(storageMap.containsKey(type)){
            return storageMap.get(type);
        } else {
            throw new NameNotFoundException(type);
        }
    }
    
    /**
     * Return blank file storage in current working directory
     * @param context
     * @return
     */
    public Storage getDefaultStorage(){
        try {
            return FileStorage.in(context.io().getRootDirectory(), null);
        } catch (StorageException ex) {
            throw new RuntimeException("Can't initialize default storage", ex);
        }
    }
    
    public Storage buildStorage(Meta annotation){
        String type = annotation.getString("type", DEFAULT_STORAGE_TYPE);
        if(context == null){
            return getStorageFactory(type).build(GlobalContext.instance(), annotation);
        } else {
            return getStorageFactory(type).build(context, annotation);
        }
    }
    
}

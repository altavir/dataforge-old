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
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;

/**
 *
 * @author darksnake
 */
@PluginDef(name = "storage", group = "hep.dataforge", description = "Basic DataForge storage plugin")
public class StorageManager extends BasicPlugin implements Encapsulated {

    /**
     * Get storage manager from given context. Attach new storage manager to
     * context if it is not provided
     *
     * @param context
     * @return
     */
    public static StorageManager buildFrom(Context context) {
        if (!context.provides("storage")) {
            context.loadPlugin(new StorageManager());
        }
        return context.provide("storage", StorageManager.class);
    }

    private Context context;

//    private final ReferenceRegistry<Storage> storageCache = new ReferenceRegistry<>();
    @Override
    public void attach(Context context) {
        this.context = context;
    }

    @Override
    public void detach(Context context) {
        this.context = null;
    }

    /**
     * Return blank file storage in current working directory
     *
     * @param context
     * @return
     */
    public Storage getDefaultStorage() {
        try {
            return FileStorage.in(context.io().getRootDirectory(), null);
        } catch (StorageException ex) {
            throw new RuntimeException("Can't initialize default storage", ex);
        }
    }

    public Storage buildStorage(Meta config) {
        Storage res = StorageFactory.buildStorage(getContext(), config);
//        storageCache.add(res);
        return res;
    }

    @Override
    public Context getContext() {
        return context;
    }

}

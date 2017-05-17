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
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static hep.dataforge.storage.api.Storage.STORAGE_TARGET;

/**
 * @author darksnake
 */
@PluginDef(name = "storage", group = "hep.dataforge", info = "Basic DataForge storage plugin")
public class StorageManager extends BasicPlugin {

    /**
     * Get storage manager from given context. Attach new storage manager to
     * context if it is not provided
     *
     * @param context
     * @return
     */
    public static StorageManager buildFrom(Context context) {
        return context.getFeature(StorageManager.class);
    }

    private Map<Meta, Storage> storageCache = new HashMap<>();

    /**
     * Return blank file storage in current working directory
     *
     * @return
     */
    public Storage getDefaultStorage() {
        try {
            return FileStorage.in(getContext().io().getRootDirectory(), null);
        } catch (StorageException ex) {
            throw new RuntimeException("Can't initialize default storage", ex);
        }
    }

    @Provides(STORAGE_TARGET)
    public Optional<Storage> optStorage(String name) {
        return storageCache.values().stream().filter(it -> Objects.equals(it.getName(), name)).findAny();
    }

    @ProvidesNames(STORAGE_TARGET)
    public Stream<String> storageNames() {
        return storageCache.values().stream().map(Named::getName).distinct();
    }

    public Storage buildStorage(Meta config) {
        //FIXME fix duplicate names
        return storageCache.computeIfAbsent(config, cfg -> StorageFactory.buildStorage(getContext(), cfg));
    }

}

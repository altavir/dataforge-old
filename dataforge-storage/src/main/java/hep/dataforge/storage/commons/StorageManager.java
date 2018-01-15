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

import hep.dataforge.context.*;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.StorageType;

import java.util.*;
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

    /**
     * Storage registry
     */
    private Map<Meta, Storage> storages = new HashMap<>();


    private static final ServiceLoader<StorageType> loader = ServiceLoader.load(StorageType.class);
    private static final String DEFAULT_STORAGE_TYPE = "file";

    private static Optional<StorageType> getStorageFactory(String type) {
        for (StorageType st : loader) {
            if (st.type().equalsIgnoreCase(type)) {
                return Optional.of(st);
            }
        }
        return Optional.empty();
    }

    public static Storage buildStorage(Context context, Meta meta) {
        return context.loadFeature("hep.dataforge:storage", StorageManager.class).buildStorage(meta);
    }


    public StorageManager(Meta meta) {
        super(meta);
        if (meta.hasMeta("storage")) {
            meta.getMetaList("storage").forEach(this::buildStorage);
        } else if(!meta.isEmpty()) {
            buildStorage(meta);
        }
    }

    public StorageManager() {
    }

    /**
     * Return blank file storage in current working directory
     *
     * @return
     */
    public Storage getDefaultStorage() {
        try {
            return storages.values().stream().findFirst().orElseGet(() -> buildStorage(Meta.empty()));

        } catch (StorageException ex) {
            throw new RuntimeException("Can't initialize default storage", ex);
        }
    }

    @Provides(STORAGE_TARGET)
    public Optional<Storage> optStorage(String name) {
        return storages.values().stream().filter(it -> Objects.equals(it.getName(), name)).findAny();
    }

    @ProvidesNames(STORAGE_TARGET)
    public Stream<String> storageNames() {
        return storages.values().stream().map(Named::getName).distinct();
    }

    public Storage buildStorage(Meta config) {
        //FIXME fix duplicate names
        return storages.computeIfAbsent(config, cfg -> {
            String type = cfg.getString("type", DEFAULT_STORAGE_TYPE);
            Optional<StorageType> factory = getStorageFactory(type);
            if (factory.isPresent()) {
                return factory.get().build(getContext(), cfg);
            } else {
                throw new RuntimeException("Can't find Storage factory for type " + type);
            }
        });
    }

    @Override
    public void detach() {
        storages.values().forEach(shelf -> {
            try {
                shelf.close();
            } catch (Exception e) {
                getLogger().error("Failed to close storage", e);
            }
        });
        storages.clear();
        super.detach();
    }

    public static class Factory implements PluginFactory {

        @Override
        public PluginTag getTag() {
            return Plugin.resolveTag(StorageManager.class);
        }

        @Override
        public Plugin build(Meta meta) {
            return new StorageManager(meta);
        }
    }
}

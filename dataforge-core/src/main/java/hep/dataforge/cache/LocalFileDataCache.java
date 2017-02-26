/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.utils.Misc;
import hep.dataforge.workspace.identity.Identity;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The data cache using local files
 *
 * @author Alexander Nozik
 */
@Deprecated
public class LocalFileDataCache extends DataCache {

    private static final String CACHE_MAP_FILE = "cache.map";

    //TODO replace by memory limited cache
    private final Map<Identity, Object> lruCache;
    private final Map<Identity, File> fileMap;
    private final File cacheDir;
    private Context context;

    public LocalFileDataCache(File cacheDir, int cacheSize) {
        this.cacheDir = cacheDir;
        lruCache = Misc.getLRUCache(cacheSize);
        fileMap = new ConcurrentHashMap<>();
        loadCacheMap();
    }

    public LocalFileDataCache(Context context, Meta meta) {
        this.context = context;
        if (meta.hasValue("directory")) {
            cacheDir = new File(meta.getString("directory"));
        } else {
            cacheDir = new File(context.io().getTmpDirectory(), ".cache");
        }
        lruCache = Misc.getLRUCache(meta.getInt("items", 300));
        fileMap = new ConcurrentHashMap<>();
        loadCacheMap();
    }

    @Override
    public Context getContext() {
        return context != null ? context : Global.instance();
    }

    private void loadCacheMap() {
        File cacheMapFile = new File(cacheDir(), CACHE_MAP_FILE);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheMapFile))) {
            fileMap.putAll((Map<? extends Identity, ? extends File>) ois.readObject());
        } catch (Exception ex) {
            getLogger().debug("Failed to load cache map from file");
        }
    }

    private void saveCacheMap() {
        File cacheMapFile = new File(cacheDir(), CACHE_MAP_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheMapFile))) {
            oos.writeObject(fileMap);
        } catch (IOException ex) {
            getLogger().error("Failed to save cache map to file", ex);
        }
    }

    private File cacheDir() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    protected <T> T restore(Identity id) throws DataCacheException {
        if (lruCache.containsKey(id)) {
            return (T) lruCache.get(id);
        } else if (fileMap.containsKey(id)) {
            File file = fileMap.get(id);
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                T res = (T) ois.readObject();
                lruCache.put(id, res);
                return res;
            } catch (Exception ex) {
                getLogger().error("Failed to read cached object with id '{}' from file with message: {}", id.toString(), ex.getMessage());
                if (file.exists()) {
                    file.deleteOnExit();
                }
                fileMap.remove(id);
                throw new DataCacheException("File read error", ex);
            }
        } else {
            throw new DataCacheException("Object not cached");
        }
    }

    @Override
    protected <T> T store(Identity id, T data) {
        //TODO add prefix here
        lruCache.put(id, data);
        if (data instanceof Serializable) {
            String fileName = data.getClass().getSimpleName();
            if (data instanceof Named) {
                fileName += "[" + ((Named) data).getName() + "]";
            }
            fileName += id.hashCode() + ".dfcache";

            File file = new File(cacheDir(), fileName);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(data);
                fileMap.put(id, file);
                saveCacheMap();
            } catch (IOException ex) {
                getLogger().error("Failed to write data with id '{}' to file with message: {}", id.toString(), ex.getMessage());
            }
        }
        return data;
    }

    @Override
    protected boolean contains(Identity id) {
        return lruCache.containsKey(id) || fileMap.containsKey(id);
    }

    @Override
    protected void invalidate(Identity id) {
        lruCache.remove(id);
        if (fileMap.containsKey(id)) {
            fileMap.get(id).delete();
        }
    }

    @Override
    public void invalidate() {
        lruCache.keySet().forEach(id -> {
            if (fileMap.containsKey(id)) {
                fileMap.get(id).delete();
            }
        });
        lruCache.clear();
    }
}

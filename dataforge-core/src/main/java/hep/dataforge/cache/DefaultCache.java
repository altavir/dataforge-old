package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.workspace.identity.Identity;

import javax.cache.Cache;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.io.File;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by darksnake on 10-Feb-17.
 */
public class DefaultCache<V> extends SimpleConfigurable implements Cache<Identity, V>, Encapsulated {

    private final String name;
    private DefaultCacheManager manager;
    private final Class<V> valueType;

    private Map<Identity, V> lruCache;
    private Map<Identity, Envelope> envelopes;
    private File cacheDir;

    public DefaultCache(String name, DefaultCacheManager manager, Class<V> valueType) {
        this.name = name;
        this.manager = manager;
        this.valueType = valueType;
    }

    @Override
    public V get(Identity id) {
        if (lruCache.containsKey(id)) {
            return (V) lruCache.get(id);
        } else if (envelopes.containsKey(id)) {
            Envelope envelpe = envelopes.get(id);
            try (ObjectInputStream ois = new ObjectInputStream(envelpe.getData().getStream())) {
                V res = (V) ois.readObject();
                lruCache.put(id, res);
                return res;
            } catch (Exception ex) {
//                getLogger().error("Failed to read cached object with id '{}' from file with message: {}", id.toString(), ex.getMessage());
//                if (envelpe instanceof FileEnvelope) {
//                    file.deleteOnExit();
//                }
                envelopes.remove(id);
                throw new RuntimeException("File read error", ex);
            }
        } else {
            throw new RuntimeException("Object not cached");
        }
    }

//    private Map<Identity, File> scan(){
//
//    }

    @Override
    public Map<Identity, V> getAll(Set<? extends Identity> keys) {
        return null;
    }

    @Override
    public boolean containsKey(Identity key) {
        return false;
    }

    @Override
    public void loadAll(Set<? extends Identity> keys, boolean replaceExistingValues, CompletionListener completionListener) {

    }

    @Override
    public synchronized void put(Identity id, V data) {
//        lruCache.put(id, data);
//        if (data instanceof Serializable) {
//            String fileName = data.getClass().getSimpleName();
//            if (data instanceof Named) {
//                fileName += "[" + ((Named) data).getName() + "]";
//            }
//            fileName += id.hashCode() + ".dfcache";
//
//            File file = new File(cacheDir(), fileName);
//            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
//                oos.writeObject(data);
//                envelopes.put(id, file);
//            } catch (IOException ex) {
//                getLogger().error("Failed to write data with id '{}' to file with message: {}", id.toString(), ex.getMessage());
//            }
//        }
    }

    @Override
    public V getAndPut(Identity key, V value) {
        return null;
    }

    @Override
    public void putAll(Map<? extends Identity, ? extends V> map) {
        map.forEach((id, data) -> put(id, data));
    }

    @Override
    public boolean putIfAbsent(Identity key, V value) {
        return false;
    }

    @Override
    public boolean remove(Identity key) {
        return false;
    }

    @Override
    public boolean remove(Identity key, V oldValue) {
        return false;
    }

    @Override
    public V getAndRemove(Identity key) {
        return null;
    }

    @Override
    public boolean replace(Identity key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public boolean replace(Identity key, V value) {
        return false;
    }

    @Override
    public V getAndReplace(Identity key, V value) {
        return null;
    }

    @Override
    public void removeAll(Set<? extends Identity> keys) {

    }

    @Override
    public void removeAll() {
        clear();
    }

    @Override
    public void clear() {

    }

    @Override
    public <T> T invoke(Identity key, EntryProcessor<Identity, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
        return null;
    }

    @Override
    public <T> Map<Identity, EntryProcessorResult<T>> invokeAll(Set<? extends Identity> keys, EntryProcessor<Identity, V, T> entryProcessor, Object... arguments) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DefaultCacheManager getCacheManager() {
        return manager;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return clazz.cast(this);
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<Identity, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Identity, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public Iterator<Entry<Identity, V>> iterator() {
        return null;
    }

    @Override
    public <C extends Configuration<Identity, V>> C getConfiguration(Class<C> clazz) {
        return clazz.cast(new MetaCacheConfiguration<V>(getConfig(), valueType));
    }

    @Override
    public Context getContext() {
        return getCacheManager().getContext();
    }
}

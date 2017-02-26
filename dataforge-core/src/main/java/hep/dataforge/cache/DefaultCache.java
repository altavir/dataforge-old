package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Named;
import hep.dataforge.utils.Misc;
import hep.dataforge.values.Value;
import hep.dataforge.workspace.identity.Identity;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Default implementation for
 * Created by darksnake on 10-Feb-17.
 */
public class DefaultCache<V> extends SimpleConfigurable implements Cache<Identity, V>, Encapsulated {

    private static DefaultEnvelopeReader reader = new DefaultEnvelopeReader();
    private static DefaultEnvelopeWriter writer = new DefaultEnvelopeWriter();

    private final String name;
    private DefaultCacheManager manager;
    private final Class<V> valueType;

    private Map<Identity, V> softCache;
    private Map<Identity, File> hardCache = new HashMap<>();
    private File cacheDir;

    public DefaultCache(String name, DefaultCacheManager manager, Class<V> valueType) {
        this.name = name;
        this.manager = manager;
        this.valueType = valueType;
        cacheDir = new File(manager.getRootCacheDir(), name);
        scanDirectory();
    }

    private Logger getLogger() {
        return getContext().getLogger();
    }

    private Envelope read(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return reader.readWithData(fis);
        } catch (Exception e) {
            throw new RuntimeException("File read error", e);
        }
    }

    private synchronized void scanDirectory() {
        if (cacheDir.exists()) {
            hardCache.clear();
            Stream.of(cacheDir.listFiles((dir, name) -> name.endsWith(".dfcache"))).forEach(file -> {
                try {
                    Envelope envelope = read(file);
                    hardCache.put(Identity.from(envelope.meta()), file);
                } catch (Exception ex) {
                    getLogger().error("Failed to read cache file {}", file.getName());
                    file.delete();
                }
            });
        }
    }

    @Override
    public V get(Identity id) {
        if (getSoftCache().containsKey(id)) {
            return (V) getSoftCache().get(id);
        } else if (hardCache.containsKey(id)) {
            File cacheFile = hardCache.get(id);
            Envelope envelope = read(cacheFile);
            try (ObjectInputStream ois = new ObjectInputStream(envelope.getData().getStream())) {
                V res = (V) ois.readObject();
                getSoftCache().put(id, res);
                return res;
            } catch (Exception ex) {
                getLogger().error("Failed to read cached object with id '{}' from file with message: {}", id.toString(), ex.getMessage());
                cacheFile.delete();
                hardCache.remove(id);
                return null;
            }
        } else {
            return null;
        }
    }


    @Override
    public Map<Identity, V> getAll(Set<? extends Identity> keys) {
        return null;
    }

    @Override
    public boolean containsKey(Identity key) {
        return getSoftCache().containsKey(key) || hardCache.containsKey(key);
    }

    @Override
    public void loadAll(Set<? extends Identity> keys, boolean replaceExistingValues, CompletionListener completionListener) {

    }

    @Override
    public synchronized void put(Identity id, V data) {
        getSoftCache().put(id, data);
        if (data instanceof Serializable) {
            String fileName = data.getClass().getSimpleName();
            if (data instanceof Named) {
                fileName += "[" + ((Named) data).getName() + "]";
            }
            fileName += Integer.toUnsignedLong(id.hashCode()) + ".dfcache";

            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File file = new File(cacheDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(data);
                EnvelopeBuilder builder = new EnvelopeBuilder().setMeta(id.toMeta()).setData(baos.toByteArray());
                baos.close();
                writer.write(fos, builder.build());
                hardCache.put(id, file);
            } catch (IOException ex) {
                getLogger().error("Failed to write data with id '{}' to file with message: {}", id.toString(), ex.getMessage());
            }
        }
    }

    @Override
    public V getAndPut(Identity key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Identity, ? extends V> map) {
        map.forEach((id, data) -> put(id, data));
    }

    @Override
    public boolean putIfAbsent(Identity key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Identity key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Identity key, V oldValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getAndRemove(Identity key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(Identity key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(Identity key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getAndReplace(Identity key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll(Set<? extends Identity> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        clear();
    }

    @Override
    public void clear() {
        if (softCache != null) {
            softCache.clear();
        }
        hardCache.values().forEach(file -> file.delete());
    }

    @Override
    public <T> T invoke(Identity key, EntryProcessor<Identity, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<Identity, EntryProcessorResult<T>> invokeAll(Set<? extends Identity> keys, EntryProcessor<Identity, V, T> entryProcessor, Object... arguments) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Identity, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry<Identity, V>> iterator() {
        return softCache.entrySet().stream()
                .<Entry<Identity, V>>map(entry -> new DefaultEntry(entry.getKey(), () -> entry.getValue()))
                .iterator();
    }


    @Override
    public <C extends Configuration<Identity, V>> C getConfiguration(Class<C> clazz) {
        return clazz.cast(new MetaCacheConfiguration<V>(getConfig(), valueType));
    }

    @Override
    public Context getContext() {
        return getCacheManager().getContext();
    }

    @Override
    protected void applyValueChange(String name, Value oldItem, Value newItem) {
        super.applyValueChange(name, oldItem, newItem);
        //update cache size preserving all of the elements
        if (Objects.equals(name, "softCache.size") && softCache != null) {
            Map<Identity, V> lru = Misc.getLRUCache(newItem.intValue());
            lru.putAll(softCache);
            softCache = lru;
        }
    }

    private synchronized Map<Identity, V> getSoftCache() {
        if (softCache == null) {
            softCache = Misc.getLRUCache(getConfig().getInt("softCache.size", 500));
        }
        return softCache;
    }

    private class DefaultEntry implements Entry<Identity, V> {

        private Identity key;
        private Supplier<V> supplier;

        public DefaultEntry(Identity key, Supplier<V> supplier) {
            this.key = key;
            this.supplier = supplier;
        }

        @Override
        public Identity getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return supplier.get();
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            return clazz.cast(this);
        }
    }
}

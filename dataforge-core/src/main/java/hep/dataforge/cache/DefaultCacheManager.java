package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.Global;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by darksnake on 08-Feb-17.
 */
public class DefaultCacheManager implements CacheManager, Encapsulated {

    private final Context context;
    private Map<String, DefaultCache> map;

    public DefaultCacheManager(Context context) {
        this.context = context;
    }

    public DefaultCacheManager() {
        this.context = Global.instance();
    }

    public File getRootCacheDir() {
        return new File(context.io().getTmpDirectory(), ".cache");
    }

    @Override
    public CachingProvider getCachingProvider() {
        return new DefaultCachingProvider();
    }

    @Override
    public URI getURI() {
        return getRootCacheDir().toURI();
    }

    @Override
    public ClassLoader getClassLoader() {
        return Caching.getDefaultClassLoader();
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) throws IllegalArgumentException {
        //TODO add configuration for cache
        return getCache(cacheName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getMap().computeIfAbsent(cacheName, name -> new DefaultCache(name, this, valueType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return getMap().computeIfAbsent(cacheName, name -> new DefaultCache(name, this, Object.class));
    }

    @Override
    public Iterable<String> getCacheNames() {
        return getMap().keySet();
    }

    @Override
    public void destroyCache(String cacheName) {
        Cache cache = getMap().get(cacheName);
        if (cache != null) {
            cache.clear();
            cache.close();
            getMap().remove(cacheName);
        }
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {
        //do nothing
    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {
        //do nothing
    }

    @Override
    public void close() {
        getMap().values().forEach(DefaultCache::close);
        map = null;
    }

    @Override
    public boolean isClosed() {
        return getMap() == null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> clazz) {
        if (clazz == DefaultCacheManager.class) {
            return (T) new DefaultCacheManager();
        } else {
            throw new IllegalArgumentException("Wrong wrapped class");
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

    private synchronized Map<String, DefaultCache> getMap() {
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        return map;
    }
}

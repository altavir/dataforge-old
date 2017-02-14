package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.Global;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by darksnake on 08-Feb-17.
 */
public class DefaultCacheManager implements CacheManager, Encapsulated {

    private Context context = Global.instance();
    private Map<String, DefaultCache> map = new ConcurrentHashMap<>();

    @Override
    public CachingProvider getCachingProvider() {
        return new DefaultCachingProvider();
    }

    @Override
    public URI getURI() {
        return URI.create("default");
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
        return getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return map.computeIfAbsent(cacheName, name -> new DefaultCache(name, this, valueType));
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return map.computeIfAbsent(cacheName, name -> new DefaultCache(name, this, Object.class));
    }

    @Override
    public Iterable<String> getCacheNames() {
        return map.keySet();
    }

    @Override
    public void destroyCache(String cacheName) {
        Cache cache = map.get(cacheName);
        if (cache != null) {
            cache.close();
            map.remove(cacheName);
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
        map.values().forEach(it -> it.close());
        map = null;
    }

    @Override
    public boolean isClosed() {
        return map == null;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz == DefaultCacheManager.class) {
            return (T) new DefaultCacheManager();
        } else {
            throw new RuntimeException("Wrong wrapped class");
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }
}

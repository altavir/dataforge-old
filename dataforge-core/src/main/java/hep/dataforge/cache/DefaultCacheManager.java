package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.ContextAware;
import hep.dataforge.context.Global;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaHolder;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by darksnake on 08-Feb-17.
 */
public class DefaultCacheManager extends MetaHolder implements CacheManager, ContextAware {

    private final Context context;
    private Map<String, DefaultCache> map;

    public DefaultCacheManager(Context context, Meta cfg) {
        super(cfg);
        this.context = context;
    }

    public DefaultCacheManager() {
        this.context = Global.instance();
    }

    public Path getRootCacheDir() {
        return context.getIo().getTmpDirectory().resolve("cache");
    }

    @Override
    public CachingProvider getCachingProvider() {
        return new DefaultCachingProvider(context);
    }

    @Override
    public URI getURI() {
        return getRootCacheDir().toUri();
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
    @SuppressWarnings("unchecked")
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) throws IllegalArgumentException {
        return new DefaultCache(cacheName, this, configuration.getValueType());
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

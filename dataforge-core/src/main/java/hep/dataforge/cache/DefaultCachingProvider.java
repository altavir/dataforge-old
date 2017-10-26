package hep.dataforge.cache;

import hep.dataforge.context.Context;
import hep.dataforge.context.ContextAware;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Properties;

/**
 * Created by darksnake on 08-Feb-17.
 */
public class DefaultCachingProvider implements CachingProvider, ContextAware {
    private final Context context;

    public DefaultCachingProvider(Context context) {
        this.context = context;
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
        return null;
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return context.getClassLoader();
    }

    @Override
    public URI getDefaultURI() {
        return null;
    }

    @Override
    public Properties getDefaultProperties() {
        return null;
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
        return null;
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void close(ClassLoader classLoader) {

    }

    @Override
    public void close(URI uri, ClassLoader classLoader) {

    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        return false;
    }

    @Override
    public Context getContext() {
        return context;
    }
}

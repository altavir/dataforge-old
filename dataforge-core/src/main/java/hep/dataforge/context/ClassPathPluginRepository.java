/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The plugin resolver that searches classpath for Plugin services and loads the
 * best one
 *
 * @author Alexander Nozik
 */
public class ClassPathPluginRepository extends AbstractPluginRepository {
    public static final String PLUGIN_LOCATION_CONTEXT_KEY = "df.pluginLocation";

    private final ServiceLoader<Plugin> loader;


    public ClassPathPluginRepository(Context context) {
        ClassLoader cl = context.getClass().getClassLoader();
        if (context.hasValue(PLUGIN_LOCATION_CONTEXT_KEY)) {
            context.logger.info("Loading plugins from {}", context.getValue(PLUGIN_LOCATION_CONTEXT_KEY));
            URL[] urls = context.getValue(PLUGIN_LOCATION_CONTEXT_KEY).listValue().stream().map(val -> {
                try {
                    return new URL(val.stringValue());
                } catch (MalformedURLException e) {
                    context.getLogger().error("Malformed plugin location", e);
                    return null;
                }
            }).filter(it -> it != null).toArray(i -> new URL[i]);
            cl = new URLClassLoader(urls, cl);
        }
        loader = ServiceLoader.load(Plugin.class, cl);
    }

    @Override
    protected Stream<Plugin> stream() {
        return StreamSupport.stream(loader.spliterator(), false);
    }
}

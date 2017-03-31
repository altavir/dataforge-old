/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.names.AlphanumComparator;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The plugin resolver that searches classpath for Plugin services and loads the
 * best one
 *
 * @author Alexander Nozik
 */
public class ClassPathPluginRepository implements PluginRepository {
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
    public Plugin get(PluginTag tag) {
        return StreamSupport.stream(loader.spliterator(), false)
                .filter(plugin -> tag.matches(plugin.getTag()))
                .sorted((p1, p2) -> -AlphanumComparator.INSTANCE.compare(p1.getTag().getVersion(), p2.getTag().getVersion()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No plugin matching criterion: " + tag.toString()));
    }

    @Override
    public List<PluginTag> listTags() {
        return StreamSupport.stream(loader.spliterator(), false).map(it -> it.getTag()).collect(Collectors.toList());
    }
}

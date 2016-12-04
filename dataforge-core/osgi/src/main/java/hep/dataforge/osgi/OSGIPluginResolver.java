package hep.dataforge.osgi;

import hep.dataforge.context.Plugin;
import hep.dataforge.context.PluginResolver;
import hep.dataforge.context.PluginTag;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by darksnake on 02-Dec-16.
 */
public class OSGIPluginResolver implements PluginResolver {

    @Override
    public Plugin getPlugin(PluginTag tag) {
        return null;
    }

    @Override
    public List<Plugin> listPlugins(Predicate<Plugin> predicate) {
        return null;
    }
}

/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.context;

import hep.dataforge.exceptions.NameNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The manager for plugin system. Should monitor plugin dependencies and locks.
 *
 * @author Alexander Nozik
 */
public class PluginManager implements Encapsulated, AutoCloseable {

    private final Map<String, Plugin> plugins = new HashMap<>();
    /**
     * A context for this plugin manager
     */
    private final Context context;
    /**
     * A class path resolver
     */
    private PluginResolver pluginResolver;

    public PluginManager(Context context) {
        this.context = context;
        pluginResolver = new ClassPathPluginResolver(context);
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    protected PluginManager getParent() {
        if (getContext().getParent() == null) {
            return null;
        } else {
            return getContext().getParent().pluginManager();
        }
    }

    public Stream<Plugin> listPlugins(boolean recursive){
        if(recursive && getParent() != null ){
            return Stream.concat(plugins.values().stream(),getParent().listPlugins(true));
        } else {
            return plugins.values().stream();
        }
    }

    public PluginResolver getPluginResolver() {
        return pluginResolver;
    }

    public void setPluginResolver(PluginResolver pluginResolver) {
        this.pluginResolver = pluginResolver;
    }

    public boolean hasPlugin(String name) {
        return plugins.containsKey(name) || (getParent() != null && getParent().hasPlugin(name));
    }

    public boolean hasPlugin(PluginTag tag) {
        return plugins.containsKey(tag.getName()) || (getParent() != null && getParent().hasPlugin(tag));
    }

    public Plugin loadPlugin(String name) {
        return loadPlugin(PluginTag.fromString(name));
    }

    /**
     * Search for loaded plugin and return it if found. Throw {@link NameNotFoundException} if it is not found
     *
     * @param name
     * @return
     */
    public Plugin getPlugin(String name) {
        if (plugins.containsKey(name)) {
            return plugins.get(name);
        } else if (getParent() != null) {
            getContext().getLogger()
                    .trace("The plugin with name `{}` not found in current context, searching parent context.", name);
            return getParent().getPlugin(name);
        } else {
            throw new NameNotFoundException(name, "Plugin not found");
        }
    }

    /**
     * Get plugin instance via plugin reolver and load it.
     *
     * @param tag
     * @return
     */
    public Plugin loadPlugin(PluginTag tag) {
        Plugin plugin = pluginResolver.getPlugin(tag);
        if (plugin == null) {
            throw new NameNotFoundException(tag.toString(), "Plugin not found");
        }
        return loadPlugin(plugin);
    }

    /**
     * Load given plugin into this manager and return loaded instance
     *
     * @param plugin
     * @param <T>
     * @return
     */
    public synchronized <T extends Plugin> T loadPlugin(T plugin) {
        if (!this.plugins.containsKey(plugin.getName())) {
            for (PluginTag tag : plugin.dependsOn()) {
                //If dependency not loaded
                if (!hasPlugin(tag)) {
                    //Load dependency
                    loadPlugin(tag);
                }
            }
            getContext().getLogger().info("Loading plugin {} into {}", plugin.getName(), context.getName());
            plugin.attach(getContext());
            plugins.put(plugin.getName(), plugin);
        } else {
            getContext().getLogger().debug("Plugin with name {} already exists in {}", plugin.getName(), getContext().getName());
        }
        return plugin;
    }

    @Override
    public void close() throws Exception {
        this.plugins.values().forEach(Plugin::detach);
    }

    /**
     * List loaded plugins
     *
     * @return
     */
    public Collection<Plugin> list() {
        return this.plugins.values();
    }
}

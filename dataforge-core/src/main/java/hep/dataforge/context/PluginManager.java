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

import java.util.HashMap;
import java.util.Map;

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
    private PluginResolver pluginResolver = new ClassPathPluginResolver();

    public PluginManager(Context context) {
        this.context = context;
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

    public PluginResolver getPluginResolver() {
        return pluginResolver;
    }

    public void setPluginResolver(PluginResolver pluginResolver) {
        this.pluginResolver = pluginResolver;
    }

    public boolean hasPlugin(String name) {
        return plugins.containsKey(name) || (getParent() != null && getParent().hasPlugin(name));
    }

    public boolean hasPlugin(VersionTag tag) {
        return plugins.containsKey(tag.name()) || (getParent() != null && getParent().hasPlugin(tag));
    }

    public Plugin loadPlugin(String name) {
        return loadPlugin(VersionTag.fromString(name));
    }

    /**
     * Search for loaded plugin and
     *
     * @param name
     * @return
     */
    public Plugin getPlugin(String name) {
        if (plugins.containsKey(name)) {
            return plugins.get(name);
        } else if (getParent() != null) {
            getContext().getLogger()
                    .debug("The plugin with name `{}` not found in current context, searching parent context.", name);
            return getParent().getPlugin(name);
        } else {
            throw new NameNotFoundException(name, "Plugin not found");
        }
    }

    public Plugin loadPlugin(VersionTag name) {
        Plugin plugin = pluginResolver.getPlugin(name);
        if (plugin == null) {
            throw new NameNotFoundException(name.getFullName(), "Plugin not found");
        }
        return loadPlugin(plugin);
    }

    public <T extends Plugin> T loadPlugin(T plugin) {
        if (!this.plugins.containsKey(plugin.getName())) {
            for (VersionTag tag : plugin.dependsOn()) {
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
            getContext().getLogger().debug("Plugin with name {} already exists in [}", plugin.getName(), getContext().getName());
        }
        return plugin;
    }

    @Override
    public void close() throws Exception {
        this.plugins.values().forEach(Plugin::detach);
    }
}

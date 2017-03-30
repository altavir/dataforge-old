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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The manager for plugin system. Should monitor plugin dependencies and locks.
 *
 * @author Alexander Nozik
 */
public class PluginManager implements Encapsulated, AutoCloseable {

    /**
     * A set of loaded plugins
     */
    private final Set<Plugin> plugins = new HashSet<>();

    /**
     * A context for this plugin manager
     */
    private final Context context;

    /**
     * A class path resolver
     */
    private PluginRepository pluginRepository;

    public PluginManager(Context context) {
        this.context = context;
        pluginRepository = new ClassPathPluginRepository(context);
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

    public Stream<Plugin> stream(boolean recursive) {
        if (recursive && getParent() != null) {
            return Stream.concat(plugins.stream(), getParent().stream(true));
        } else {
            return plugins.stream();
        }
    }

    public PluginRepository getPluginRepository() {
        return pluginRepository;
    }

    public void setPluginRepository(PluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public boolean hasPlugin(String name) {
        return optPlugin(PluginTag.fromString(name)).isPresent();
    }

    public boolean hasPlugin(PluginTag tag) {
        return optPlugin(tag).isPresent();
    }

    public Optional<Plugin> optPlugin(PluginTag tag) {
        //Check for ambiguous tag
        if (stream(false).filter(it -> tag.matches(it.getTag())).count() > 1) {
            getContext().getLogger().warn("Ambiguous plugin resolution with tag {}", tag);
        }
        return stream(true).filter(it -> tag.matches(it.getTag())).findFirst();
    }

    /**
     * Search for loaded plugin and return it if found. Throw {@link NameNotFoundException} if it is not found
     *
     * @param name
     * @return
     */
    public Plugin getPlugin(String name) {
        return optPlugin(PluginTag.fromString(name)).get();
    }

    /**
     * Load given plugin into this manager and return loaded instance
     *
     * @param plugin
     * @param <T>
     * @return
     */
    public synchronized <T extends Plugin> T loadPlugin(T plugin) {
        if (!this.plugins.stream().anyMatch(it -> it.getTag().matches(plugin.getTag()))) {
            for (PluginTag tag : plugin.dependsOn()) {
                //If dependency not loaded
                if (!hasPlugin(tag)) {
                    //Load dependency
                    loadPlugin(tag);
                }
            }
            getContext().getLogger().info("Loading plugin {} into {}", plugin.getName(), context.getName());
            plugin.attach(getContext());
            plugins.add(plugin);
        } else {
            getContext().getLogger().warn("Plugin with tag {} already exists in {}", plugin.getTag(), getContext().getName());
        }
        return plugin;
    }

    /**
     * Get plugin instance via plugin reolver and load it.
     *
     * @param tag
     * @return
     */
    public Plugin loadPlugin(PluginTag tag) {
        Plugin plugin = pluginRepository.get(tag);
        if (plugin == null) {
            throw new NameNotFoundException(tag.toString(), "Plugin not found");
        }
        return loadPlugin(plugin);
    }

    public Plugin loadPlugin(String name) {
        return loadPlugin(PluginTag.fromString(name));
    }

    public Plugin getOrLoadPlugin(PluginTag tag) {
        return optPlugin(tag).orElseGet(() -> pluginRepository.get(tag));
    }

    public Plugin getOrLoadPlugin(String tag) {
        return getOrLoadPlugin(PluginTag.fromString(tag));
    }


    @Override
    public void close() throws Exception {
        this.plugins.forEach(Plugin::detach);
    }

    /**
     * List loaded plugins
     *
     * @return
     */
    public Collection<Plugin> list() {
        return this.plugins;
    }
}

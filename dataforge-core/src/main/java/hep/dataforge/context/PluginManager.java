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
        if (context == null) {
            return Global.getDefaultContext();
        }
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

    public boolean has(String name) {
        return opt(PluginTag.fromString(name)).isPresent();
    }

    public boolean has(PluginTag tag) {
        return opt(tag).isPresent();
    }

    /**
     * Find a loaded plugin
     *
     * @param tag
     * @return
     */
    public Optional<Plugin> opt(PluginTag tag) {
        //Check for ambiguous tag
        if (stream(false).filter(it -> tag.matches(it.getTag())).count() > 1) {
            getContext().getLogger().warn("Ambiguous plugin resolution with tag {}", tag);
        }
        return stream(true).filter(it -> tag.matches(it.getTag())).findFirst();
    }

    /**
     * Find a loaded plugin and cast it to a specific plugin class
     *
     * @param tag
     * @param type
     * @param <T>
     * @return
     */
    public <T extends Plugin> Optional<T> opt(PluginTag tag, Class<T> type) {
        return opt(tag).map(it -> type.cast(it));
    }

    public <T extends Plugin> Optional<T> opt(Class<T> type) {
        return this.<T>opt(Plugin.resolveTag(type), type);
    }

    /**
     * Load given plugin into this manager and return loaded instance
     *
     * @param plugin
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Plugin> T load(T plugin) {
        Optional<Plugin> loadedPlugin = opt(plugin.getTag());

        if (loadedPlugin.isPresent()) {
            getContext().getLogger().warn("Plugin with tag {} already exists in {}", plugin.getTag(), getContext().getName());
            return (T) loadedPlugin.get();
        } else {
            for (PluginTag tag : plugin.dependsOn()) {
                //If dependency not loaded
                if (!has(tag)) {
                    //Load dependency
                    load(tag);
                }
            }
            getContext().getLogger().info("Loading plugin {} into {}", plugin.getName(), context.getName());
            plugin.attach(getContext());
            plugins.add(plugin);
            return plugin;
        }
    }

    /**
     * Get plugin instance via plugin reolver and load it.
     *
     * @param tag
     * @return
     */
    public Plugin load(PluginTag tag) {
        return load(pluginRepository.opt(tag)
                .orElseThrow(() -> new NameNotFoundException(tag.toString(), "Plugin not found"))
        );
    }

    public <T extends Plugin> T load(Class<T> type) {
        PluginTag tag = Plugin.resolveTag(type);
        T plugin;
        try {
            plugin = type.cast(load(tag));
        } catch (NameNotFoundException ex) {
            getContext().getLogger().warn("The plugin with tag {} not found in the repository. Trying to create instance directly.", tag);
            try {
                plugin = type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Can't build an instance of the plugin " + type.getName());
            }
        }
        return plugin;
    }

    public Plugin load(String name) {
        return load(PluginTag.fromString(name));
    }

    /**
     * Get a plugin if it is loaded in this manager or parent manager. If it is not, load and get it.
     *
     * @param tag
     * @return
     */
    public Plugin getOrLoad(PluginTag tag) {
        return opt(tag).orElseGet(() -> load(pluginRepository.get(tag)));
    }

    public Plugin getOrLoad(String tag) {
        return getOrLoad(PluginTag.fromString(tag));
    }

    public <T extends Plugin> T getOrLoad(Class<T> type) {
        return opt(Plugin.resolveTag(type)).map(type::cast).orElseGet(() -> load(type));
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

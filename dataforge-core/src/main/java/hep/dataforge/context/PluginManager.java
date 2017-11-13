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

import hep.dataforge.exceptions.ContextLockException;
import hep.dataforge.exceptions.NameNotFoundException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The manager for plugin system. Should monitor plugin dependencies and locks.
 *
 * @author Alexander Nozik
 */
public class PluginManager implements ContextAware, AutoCloseable {


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
            return getContext().getParent().getPluginManager();
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

    private Optional<Plugin> opt(Predicate<Plugin> predicate, boolean recursive) {
        List<Plugin> plugins = stream(false).filter(predicate).collect(Collectors.toList());
        if (plugins.size() == 0) {
            if (recursive && getParent() != null) {
                return getParent().opt(predicate, true);
            } else {
                return Optional.empty();
            }
        } else if (plugins.size() == 1) {//Check for ambiguous tag
            return Optional.of(plugins.get(0));
        } else {
            throw new RuntimeException("Ambiguous plugin resolution");
        }
    }

    /**
     * Search for a plugin inside current context
     *
     * @param tag
     * @return
     */
    public Optional<Plugin> optInContext(PluginTag tag) {
        return opt(it -> tag.matches(it.getTag()), false);
    }

    /**
     * Find a loaded plugin
     *
     * @param tag
     * @return
     */
    public Optional<Plugin> opt(PluginTag tag) {
        return opt(it -> tag.matches(it.getTag()), true);
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
        return opt(tag).map(type::cast);
    }

    public <T extends Plugin> Optional<T> opt(Class<T> type) {
        return opt(type::isInstance,true).map(type::cast);
    }

    /**
     * Load given plugin into this manager and return loaded instance
     *
     * @param plugin
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Plugin> T load(T plugin) {
        if (getContext().isLocked()) {
            throw new ContextLockException();
        }

        Optional<Plugin> loadedPlugin = optInContext(plugin.getTag());

        if (loadedPlugin.isPresent()) {
            getLogger().warn("Plugin with tag {} already exists in {}", plugin.getTag(), getContext().getName());
            return (T) loadedPlugin.get();
        } else {
            for (PluginTag tag : plugin.dependsOn()) {
                //If dependency not loaded
                if (!has(tag)) {
                    //Load dependency
                    load(tag);
                }
            }

            getLogger().info("Loading plugin {} into {}", plugin.getName(), context.getName());
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

    public <T extends Plugin> T load(Class<T> type, Consumer<T> initializer) {
        PluginTag tag = Plugin.resolveTag(type);
        T plugin;
        try {
            plugin = type.cast(getPluginRepository().get(tag));
        } catch (Exception ex) {
            getLogger().debug("The plugin with tag {} not found in the repository. Trying to create instance directly.", tag);
            try {
                plugin = type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Can't builder an instance of the plugin " + type.getName());
            }
        }
        initializer.accept(plugin);
        return load(plugin);
    }

    public <T extends Plugin> T load(Class<T> type) {
        return load(type,
                t -> {
                }
        );
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

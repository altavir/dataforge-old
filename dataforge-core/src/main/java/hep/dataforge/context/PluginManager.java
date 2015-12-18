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

import hep.dataforge.io.log.Logable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The manager for plugin system. Should monitor plugin dependencies and locks.
 *
 * @author Alexander Nozik
 */
public class PluginManager implements Confined {

    private final Context context;

    private final Set<Plugin> plugins = new HashSet<>();

    public PluginManager(Context context) {
        this.context = context;
    }
    
    protected PluginManager getParent(){
        if(getContext().getParent() == null){
            return null;
        } else {
            return getContext().getParent().pluginManager();
        }
    }

    public boolean addPlugin(Plugin plugin, boolean autoLoadDependencies) {
        //Checking dependencies        
        for (VersionTag tag : plugin.dependsOn()) {
            //If dependency not loaded
            if (!hasPlugin(tag)) {
                if (autoLoadDependencies && getPluginLibrary().has(tag)) {
                    //Load dependency from library
                    Plugin dependency = buildFromLibrary(tag);
                    if (!addPlugin(dependency, autoLoadDependencies)) {
                        //Check if dependency is safely loaded
                        getContext().logError("Can't start plugin {}", dependency.name());
                        return false;
                    }
                } else {
                    //If dependency not found or autoloading is forbiden
                    getContext().logError("Can't start plugin {}. The dependency {} not found",
                            plugin.name(), tag.getFullName());
                    return false;
                }
            }
        }
        if (!hasPlugin(plugin.getTag())) {
            plugin.apply(getContext());
            plugins.add(plugin);
        }
        return true;
    }

    private Plugin buildFromLibrary(VersionTag tag) {
        if (getPluginLibrary().has(tag)) {
            return getPluginLibrary().get(tag);
        } else {
            return null;
        }
    }

    public boolean removePlugin(Plugin plugin) {
        if (plugins.contains(plugin)) {
            //TODO сделать проверку использования плагина
            plugin.clean(getContext());
            plugins.remove(plugin);
            return true;
        } else {
            getContext().logError("The plugin {} is not loaded and can not be stopped", plugin.name());
            return false;
        }
    }

    public boolean addPluginFromLibrary(VersionTag tag) {
        if (getPluginLibrary().has(tag)) {
            return this.addPlugin(buildFromLibrary(tag), true);
        } else {
            getContext().logError("The plugin {} not found in the library", tag.getFullName());
            return false;
        }
    }

    public boolean removePlugin(VersionTag tag) {
        if (this.hasPlugin(tag)) {
            return this.removePlugin(getPlugin(tag));
        } else {
            return false;
        }
    }

    public boolean hasPlugin(String name) {
        return hasPlugin(VersionTag.fromString(name));
    }

    public boolean hasPlugin(VersionTag tag) {
        return getAllPlugins().stream().anyMatch((plugin) -> tag.matches(plugin.getTag()));
    }

    /**
     * Find the plugin among given plugins which suits given tag best. By
     * default searches for the latest version of plugin.
     *
     * @param tag
     * @param plugins
     * @return
     */
    protected Plugin resolve(VersionTag tag, List<Plugin> plugins) {
        Plugin res = plugins.get(0);
        for (Plugin plugin : plugins) {
            if (plugin.getTag().version().compareToIgnoreCase(res.getTag().version()) > 0) {
                res = plugin;
            }
        }
        return res;
    }
    
    /**
     * get all plugins including the ones form the parent
     * @return 
     */
    private Set<Plugin> getAllPlugins(){
        Set<Plugin> allPlugins = new LinkedHashSet<>(this.plugins);
        if(getParent()!=null){
            allPlugins.addAll(getParent().getAllPlugins());
        }
        return allPlugins;
    }

    public Plugin getPlugin(VersionTag tag) {
        List<Plugin> found = getAllPlugins().stream().filter((plugin) -> tag.matches(plugin.getTag())).collect(Collectors.toList());
        if (found.size() == 1) {
            return found.get(0);
        } else if (found.isEmpty()) {
            return null;
        } else {
            getContext().log("Multiple results for plugin lookup. The queried name is %s. Found following plugins: %s",
                    tag.getFullName(),
                    found.stream().<String>map((pl) -> pl.name()).collect(Collectors.joining(", "))
            );
            return resolve(tag, found);
        }
    }
    
//    /**
//     * Return first appearance of plugin with the given class
//     * @param <T>
//     * @param type 
//     */
//    public <T extends Plugin> T getPlugin(Class<T> type){
//        return (T) plugins.stream().filter((pl)->type.isInstance(pl)).findFirst().get();
//    }
//    
//    /**
//     * Check if there is at least one plugin of given type
//     * @param <T>
//     * @param type
//     * @return 
//     */
//    public <T extends Plugin> boolean hasPlugin(Class<T> type){
//        return plugins.stream().anyMatch((pl)->type.isInstance(pl));
//    }

    public Plugin getPlugin(String name) {
        return getPlugin(VersionTag.fromString(name));
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    /**
     * @return the pluginLibrary
     */
    public Library<Plugin> getPluginLibrary() {
        return PluginLibrary.getInstance();
    }

}

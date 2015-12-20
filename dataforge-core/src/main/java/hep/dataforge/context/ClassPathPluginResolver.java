/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.util.ServiceLoader;

/**
 * The plugin resolver that searches classpath for Plugin services and loads the
 * best one
 *
 * @author Alexander Nozik
 */
public class ClassPathPluginResolver implements PluginResolver {
    
    private static final ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);

    @Override
    public Plugin getPlugin(VersionTag tag) {
        for(Plugin plugin: loader){
            if(tag.matches(plugin.getTag())){
                return plugin;
            }
        }
        return null;
    }

}

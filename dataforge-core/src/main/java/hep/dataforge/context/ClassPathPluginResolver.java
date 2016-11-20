/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * The plugin resolver that searches classpath for Plugin services and loads the
 * best one
 *
 * @author Alexander Nozik
 */
public class ClassPathPluginResolver implements PluginResolver {
    
    private static final ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);

    @Override
    public Plugin getPlugin(PluginTag tag) {
       return StreamSupport.stream(loader.spliterator(),false)
               .filter(plugin -> tag.matches(plugin.getTag()))
               .sorted((o1, o2) -> o1.getTag().compareTo(o2.getTag()))
               .findFirst()
               .orElseThrow(()-> new RuntimeException("No plugin matching criterion: " + tag.toString()));
    }

}

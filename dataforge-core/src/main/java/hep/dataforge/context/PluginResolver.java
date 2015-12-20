/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

/**
 *  A resolution strategy for plugins
 *
 * @author Alexander Nozik
 */
public interface PluginResolver {
    Plugin getPlugin(VersionTag tag);
    
    default Plugin getPlugin(String name){
        return getPlugin(VersionTag.fromString(name));
    }
}

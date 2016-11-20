/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

/**
 * A resolution strategy for plugins
 *
 * @author Alexander Nozik
 */
public interface PluginResolver {

//    /**
//     * Get a list of all available plugins that match given tag
//     *
//     * @param tag
//     * @return
//     */
//    List<Plugin> listPlugins(PluginTag tag);
//
//    /**
//     * Load plugin by its tag
//     *
//     * @param tag
//     * @return
//     */
//    default Plugin getPlugin(PluginTag tag) {
//        List<Plugin> plugins = listPlugins(tag);
//        if (plugins.isEmpty()) {
//            throw new IllegalStateException("Plugin with tag " + tag + "not found");
//        }
//        return listPlugins(tag).get(0);
//    }

    /**
     * Load the most suitable plugin of all provided by its tag
     *
     * @param tag
     * @return
     */
    Plugin getPlugin(PluginTag tag);

    default Plugin getPlugin(String name) {
        return getPlugin(PluginTag.fromString(name));
    }
}

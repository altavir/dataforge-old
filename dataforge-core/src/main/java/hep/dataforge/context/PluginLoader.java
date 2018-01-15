/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.meta.Meta;

import java.util.List;
import java.util.Optional;

/**
 * A resolution strategy for plugins
 *
 * @author Alexander Nozik
 */
public interface PluginLoader {

    /**
     * Load the most suitable plugin of all provided by its tag
     *
     * @param tag
     * @return
     */
    Optional<Plugin> opt(PluginTag tag, Meta meta);

    default Plugin get(PluginTag tag, Meta meta) {
        return opt(tag, meta).orElseThrow(() -> new RuntimeException("No plugin matching " + tag.toString()));
    }

    /**
     * List tags provided by this repository
     *
     * @return
     */
    List<PluginTag> listTags();
}

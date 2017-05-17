/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.util.List;
import java.util.Optional;

/**
 * A resolution strategy for plugins
 *
 * @author Alexander Nozik
 */
public interface PluginRepository {

    /**
     * Load the most suitable plugin of all provided by its tag
     *
     * @param tag
     * @return
     */
    Optional<Plugin> opt(PluginTag tag);

    default Plugin get(PluginTag tag) {
        return opt(tag).orElseThrow(() -> new RuntimeException("No plugin matching " + tag.toString()));
    }

    /**
     * List tags provided by this repository
     *
     * @return
     */
    List<PluginTag> listTags();
}

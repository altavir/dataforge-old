/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.util.List;

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
    Plugin get(PluginTag tag);

    /**
     * List tags provided by this repository
     * @return
     */
    List<PluginTag> listTags();
}

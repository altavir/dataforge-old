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

import hep.dataforge.cache.Identifiable;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;
import hep.dataforge.providers.Provider;

import static hep.dataforge.meta.MetaNode.DEFAULT_META_NAME;

/**
 * The interface to define a Context plugin. A plugin stores all runtime features of a context.
 * The plugin is by default configurable and a Provider (both features could be ignored).
 * The plugin must in most cases have an empty constructor in order to be able to load it from library.
 * <p>
 * The plugin lifecycle is the following:
 * <p>
 * create - configure - attach - detach - destroy
 * <p>
 * Configuration of attached plugin is possible for a context which is not in a runtime mode, but it is not recommended.
 *
 * @author Alexander Nozik
 */
public interface Plugin extends Named, Metoid, ContextAware, Provider, Identifiable {

    String PLUGIN_TARGET = "plugin";

    /**
     * Plugin dependencies which are required to attach this plugin. Plugin
     * dependencies must be initialized and enabled in the Context before this
     * plugin is enabled.
     *
     * @return
     */
    PluginTag[] dependsOn();

    /**
     * Start this plugin and attach registration info to the context. This method
     * should be called only via PluginManager to avoid dependency issues.
     *
     * @param context
     */
    void attach(Context context);

    /**
     * Stop this plugin and remove registration info from context and other
     * plugins. This method should be called only via PluginManager to avoid
     * dependency issues.
     */
    void detach();

    /**
     * Get tag for this plugin
     *
     * @return
     */
    PluginTag getTag();

    /**
     * The name of this plugin ignoring version and group
     *
     * @return
     */
    @Override
    default String getName() {
        return getTag().getName();
    }


    @Override
    default Meta getIdentity() {
        MetaBuilder id = new MetaBuilder("id");
        id.putValue("name", this.getName());
        id.putValue("type", this.getClass().getName());
        id.putValue("context", getContext().getName());
        id.putNode(DEFAULT_META_NAME, getMeta());
        return id;
    }
}

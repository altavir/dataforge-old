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

import hep.dataforge.content.Named;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The interface to define a Context plugin. A plugin could be loaded into the
 * Context and then enabled or disabled in runtime.
 *
 * @author Alexander Nozik
 */
public interface Plugin extends Annotated, Named {

    /**
     * Plugin dependencies which are required to apply this plugin. Plugin
     * dependencies must be initialized and enabled in the Context before this
     * plugin is enabled.
     *
     * @return
     */
    VersionTag[] dependsOn();

    /**
     * Start this plugin and apply registration info to the context. This method
     * should be called only via PluginManager to avoid dependency issues.
     *
     * @param context
     */
    void apply(Context context);

    /**
     * Stop this plugin and remove registration info from context and other
     * plugins. This method should be called only via PluginManager to avoid
     * dependency issues.
     *
     * @param context
     */
    void clean(Context context);

    /**
     * Get tag for this plugin
     *
     * @return
     */
    VersionTag getTag();

    /**
     * The name of this plugin ignoring version and group
     *
     * @return
     */
    @Override
    public default String getName() {
        return getTag().name();
    }

    /**
     * Set config for this plugin and apply changes
     *
     * @param config
     */
    void configure(Meta config);

    public default String name() {
        return getTag().getFullName();
    }

    /**
     * Return new blank instance of this plugin. This method is used only to
     * avoid separate factory class;
     *
     * @return
     */
    default Plugin instance() {
        //FIXME a bad solution
        try {
            return getClass().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create instance of the plugin", ex);
        }
    }

}

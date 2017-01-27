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

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Configurable;
import hep.dataforge.names.Named;

/**
 * The interface to define a Context plugin. A plugin could be loaded into the
 * Context and then enabled or disabled in runtime.
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "priority", type = "NUMBER", info = "Plugin load priority. Used for plugins with the same role")
public interface Plugin extends Annotated, Named, Encapsulated, Configurable {

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
     *
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

//    /**
//     * Return new blank instance of this plugin. This method is used only to
//     * avoid separate factory class;
//     *
//     * @return
//     */
//    default Plugin newInstance() {
//        //FIXME a bad solution
//        try {
//            return getClass().getDeclaredConstructor().newInstance();
//        } catch (Exception ex) {
//            throw new RuntimeException("Failed to create instance of the plugin", ex);
//        }
//    }

}

/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.context

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaHolder

/**
 * A base for plugin implementation
 *
 * @author Alexander Nozik
 */
abstract class BasicPlugin(meta: Meta = Meta.empty()) : MetaHolder(meta), Plugin {

    private var context: Context? = null

    override fun dependsOn(): Array<PluginTag> {
        return if (tag.hasValue("dependsOn")) {
            tag.getStringArray("dependsOn").map { PluginTag.fromString(it) }.toTypedArray()
        } else {
            emptyArray()
        }
    }

    /**
     * If tag is not defined, then the name of class is used
     *
     * @return
     */
    override fun getTag(): PluginTag {
        return PluginTag.resolve(javaClass)
    }

    /**
     * Load this plugin to the Global without annotation
     */
    fun startGlobal() {
        if (context != null && Global != getContext()) {
            Global.logger.warn("Loading plugin as global from non-global context")
        }
        Global.pluginManager.load(this)
    }

    override fun attach(context: Context) {
        this.context = context
    }

    override fun detach() {
        this.context = null
    }

    override fun getContext(): Context {
        return context ?: throw RuntimeException("Plugin not attached")
    }
}

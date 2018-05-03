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

package hep.dataforge.storage

import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Plugin
import hep.dataforge.context.PluginDef
import hep.dataforge.context.PluginFactory
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.meta.Meta


@PluginDef(name = "storage", group = "hep.dataforge", info = "Dataforge root storage plugin")
class StorageManager() : BasicPlugin(), MutableStorage {

    override val parent: StorageElement? = null
    private val _connectionHelper = ConnectionHelper(this)
    private val _children = HashMap<String, StorageElement>()

    override val children: Map<String, StorageElement> = _children

    override fun getConnectionHelper(): ConnectionHelper = _connectionHelper

    override suspend fun createElement(meta: Meta): StorageElement {
        val type = meta.getString("type", DEFAULT_STORAGE_TYPE)
        val element = context.findService(StorageFactory::class.java) { it.name == type }
                ?.createElement(this, meta)
                ?: throw NameNotFoundException("Storage factory with type $type not found in ${context.name}")
        //TODO evaluate meta clash
        _children.putIfAbsent(element.name, element)
        return element
    }

    override fun detach() {
        super.detach()
        close()
    }

    class Factory : PluginFactory() {

        override val type: Class<out Plugin>
            get() = StorageManager::class.java

        override fun build(meta: Meta): Plugin {
            return StorageManager()
        }
    }

    companion object {
        const val DEFAULT_STORAGE_TYPE = ""
    }
}
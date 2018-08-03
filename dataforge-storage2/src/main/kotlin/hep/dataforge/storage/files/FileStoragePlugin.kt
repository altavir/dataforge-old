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

package hep.dataforge.storage.files

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Plugin
import hep.dataforge.context.PluginDef
import hep.dataforge.context.PluginFactory
import hep.dataforge.meta.Meta
import hep.dataforge.providers.Provides
import hep.dataforge.providers.ProvidesNames
import kotlin.streams.toList

@PluginDef(name = "storage.file", group = "hep.dataforge", info = "File storage type manager")
class FileStoragePlugin: BasicPlugin() {

    private val types by lazy {
        context.serviceStream(FileStorageElementType::class.java).toList() +
        listOf(FileStorage.Directory, TableLoaderType)
    }

    @Provides(FILE_STORAGE_TYPE_TARGET)
    fun optType(name:String): FileStorageElementType?{
        return types.find { it.name == name }
    }

    @ProvidesNames(FILE_STORAGE_TYPE_TARGET)
    fun listTypes(): List<String>{
        return types.map { it.name }
    }

    class Factory: PluginFactory() {
        override val type: Class<out Plugin>
            get() = FileStoragePlugin::class.java

        override fun build(meta: Meta): Plugin {
            return FileStoragePlugin()
        }
    }

    companion object {
        const val FILE_STORAGE_TYPE_TARGET = "fileStorageType"
    }
}
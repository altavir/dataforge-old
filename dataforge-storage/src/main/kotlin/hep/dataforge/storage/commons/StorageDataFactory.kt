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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons

import hep.dataforge.context.Context
import hep.dataforge.data.Data
import hep.dataforge.data.DataFactory
import hep.dataforge.data.DataNodeEditor
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Loader

/**
 * @author Alexander Nozik
 */
class StorageDataFactory : DataFactory<Loader>(Loader::class.java) {

    override val name= "storage"

    override fun fill(builder: DataNodeEditor<Loader>, context: Context, meta: Meta) {
        //FIXME this process takes long time for large storages. Need to wrap it in process
        val storage = StorageManager.buildFrom(context).buildStorage(meta)
        StorageUtils.loaderStream(storage).forEach { loader -> builder.putData(loader.fullName.toUnescaped(), Data.buildStatic(loader, loader.meta)) }
    }

}

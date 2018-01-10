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

package hep.dataforge.kodex

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginDef
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlin.coroutines.experimental.CoroutineContext

@PluginDef(group = "hep.dataforge", name = "hep/dataforge/kodex", info = "Kodex coroutine context and other useful things")
class KodexPlugin : BasicPlugin() {

    val dispatcher: CoroutineContext
        get() {
            return context.getParallelExecutor().asCoroutineDispatcher()
        }

    override fun attach(context: Context) {
        super.attach(context)
        context.logger.debug("Switching KODEX coroutine dispatcher to context executor")
//        dispatcher = context.getParallelExecutor().asCoroutineDispatcher()
    }

    override fun detach() {
        super.detach()
//        dispatcher = DefaultDispatcher
    }
}
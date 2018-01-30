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

import hep.dataforge.io.history.Chronicle
import hep.dataforge.io.history.History
import hep.dataforge.names.Name
import hep.dataforge.providers.Provides
import java.util.*

class Chronicler : BasicPlugin(), History {

    private val root: Chronicle by lazy {
        Chronicle(context.name)
    }

    override fun getChronicle(): Chronicle = root


    private val historyCache = HashMap<String, Chronicle>()

    @Provides(Chronicle.CHRONICLE_TARGET)
    fun optChronicle(logName: String): Optional<Chronicle> {
        return Optional.ofNullable(historyCache[logName])
    }

    /**
     * get or builder current log creating the whole log hierarchy
     *
     * @param reportName
     * @return
     */
    fun getChronicle(reportName: String): Chronicle {
        return historyCache.computeIfAbsent(reportName) { str ->
            val name = Name.of(str)
            val parent: History
            parent = if (name.length > 1) {
                getChronicle(name.cutLast().toString())
            } else {
                this@Chronicler
            }
            Chronicle(name.last.toString(), parent)
        }
    }


}
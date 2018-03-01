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
package hep.dataforge.storage.api

import hep.dataforge.events.Event
import hep.dataforge.events.EventHandler

/**
 * The loader for events. By default it does not provide any pull operations and
 * could ignore events partially or store only limited number of entries
 *
 * @author darksnake
 */

interface EventLoader : Loader, Iterable<Event>, EventHandler {

    override val type: String
        get() = EVENT_LOADER_TYPE

    override fun pushEvent(event: Event): Boolean

    companion object {

        const val EVENT_LOADER_TYPE = "event"
    }
}

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
package hep.dataforge.io

import hep.dataforge.context.Plugin
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.output.Output
import hep.dataforge.io.output.Output.Companion.TEXT_MODE
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name

/**
 *
 *
 * IOManager interface.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
interface OutputManager : Plugin {

    /**
     * Get secondary output for this context
     * @param stage
     * @param name
     * @return
     */
    @ValueDefs(
            ValueDef(key = "stage", def = "", info = "Fully qualified name of the output stage"),
            ValueDef(key = "name", required = true, info = "Fully qualified name of the output inside the stage if it is present"),
            ValueDef(key = "type", def = TEXT_MODE, info = "Type of the output container")
    )
    operator fun get(meta: Meta): Output {
        val name = Name.of(meta.getString("name"))
        val stage = Name.of(meta.getString("stage", ""))
        val type = meta.getString("type", Output.TEXT_MODE)
        return get(name, stage, type)
    }

    /**
     * List of all available output modes ad dataforge object type strings. By convention [TEXT_MODE] should be always available.
     */
    val outputModes: Collection<String>

    /**
     * Helper method to access output
     */
    operator fun get(name: Name, stage: Name = Name.empty(), mode: String = TEXT_MODE): Output

    //TODO provide outputs

    /**
     *
     */
    @JvmDefault
    operator fun get(name: String, stage: String = "", vararg modes: String = arrayOf(TEXT_MODE)): Output {
        val mode = modes.find { outputModes.contains(it) } ?: TEXT_MODE
        return get(Name.of(name), Name.of(stage), mode)
    }

    companion object {

        const val LOGGER_APPENDER_NAME = "df.output"
        //const val OUTPUT_STAGE_TARGET = "stage"
        //const val OUTPUT_TARGET = "output"
    }
}


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

import hep.dataforge.context.BasicPlugin
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.output.Output
import hep.dataforge.io.output.Output.Companion.TEXT_TYPE
import hep.dataforge.io.output.StreamConsumer
import hep.dataforge.kodex.buildMeta
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import java.io.OutputStream

/**
 *
 *
 * IOManager interface.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
abstract class OutputManager(meta: Meta) : BasicPlugin(meta) {


    /**
     * Get primary output for this context
     * @return
     */
    abstract val primary: Output

    /**
     * Get secondary output for this context
     * @param stage
     * @param name
     * @return
     */
    @ValueDefs(
            ValueDef(key = "stage", def = "", info = "Fully qualified name of the output stage"),
            ValueDef(key = "name", required = true, info = "Fully qualified name of the output inside the stage if it is present"),
            ValueDef(key = "type", def = TEXT_TYPE, info = "Type of the output container")
    )
    abstract fun get(meta: Meta): Output


    /**
     * Helper method to access output
     */
    @JvmOverloads
    operator fun get(name: Name, stage: Name = Name.empty(), type: String = TEXT_TYPE): Output {
        val meta = buildMeta("output") {
            "stage" to stage.toUnescaped()
            "name" to name.toUnescaped()
            "type" to type
        }
        return get(meta)
    }

    @JvmOverloads
    operator fun get(name: String, stage: String = "", type: String = TEXT_TYPE): Output {
        val meta = buildMeta("output") {
            "stage" to stage
            "name" to name
            "type" to type
        }
        return get(meta)
    }

    val stream: OutputStream by lazy { StreamConsumer(primary) }

    /**
     * An [OutputStream] wrapper for backward compatibility.
     */
    fun stream(meta: Meta): OutputStream {
        return StreamConsumer(get(meta))
    }

    /**
     * An [OutputStream] wrapper for backward compatibility.
     */
    @JvmOverloads
    fun stream(name: Name, stage: Name = Name.empty(), type: String = TEXT_TYPE): OutputStream {
        return StreamConsumer(get(name, stage, type))
    }

    @JvmOverloads
    fun stream(name: String, stage: String = "", type: String = TEXT_TYPE): OutputStream {
        val meta = buildMeta("output") {
            "stage" to stage
            "name" to name
            "type" to type
        }
        return StreamConsumer(get(meta))
    }

    companion object {
        //const val BINARY_TARGET = "bin"
        //const val RESOURCE_TARGET = "resource"
        //const val FILE_TARGET = "file"
        //    String RESOURCE_TARGET = "resource";

        const val LOGGER_APPENDER_NAME = "df.output"
    }

}

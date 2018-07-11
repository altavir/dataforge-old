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
import hep.dataforge.context.Context
import hep.dataforge.context.Plugin
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.output.Output
import hep.dataforge.io.output.Output.Companion.splitOutput
import hep.dataforge.meta.KMetaBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta

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
            ValueDef(key = OUTPUT_STAGE_KEY, def = "", info = "Fully qualified name of the output stage"),
            ValueDef(key = OUTPUT_NAME_KEY, required = true, info = "Fully qualified name of the output inside the stage if it is present"),
            ValueDef(key = OUTPUT_MODE_KEY, def = "", info = "Type of the output container")
    )
    fun get(meta: Meta): Output

    /**
     * Render a single object to output, using provided meta both for finding the output and output properties.
     * Method could be overridden to infer render mode.
     */
    @JvmDefault
    fun render(meta: Meta, obj: Any) {
        get(meta).render(obj, meta)
    }

    /**
     * Kotlin helper
     */
    @JvmDefault
    fun get(action: KMetaBuilder.() -> Unit): Output = get(buildMeta("output", action))

    /**
     *
     */
    @JvmDefault
    operator fun get(stage: String, name: String, mode: String? = null): Output {
        return get {
            OUTPUT_NAME_KEY to name
            OUTPUT_STAGE_KEY to stage
            OUTPUT_MODE_KEY to mode
        }
    }


    @JvmDefault
    operator fun get(name: String): Output {
        return get {
            OUTPUT_NAME_KEY to name
        }
    }

    companion object {

        const val LOGGER_APPENDER_NAME = "df.output"
        const val OUTPUT_STAGE_KEY = "stage"
        const val OUTPUT_NAME_KEY = "name"
        const val OUTPUT_MODE_KEY: String = "mode"
        //const val OUTPUT_STAGE_TARGET = "stage"
        //const val OUTPUT_TARGET = "output"

        /**
         * Produce a split [OutputManager]
         */
        fun split(vararg channels: OutputManager): OutputManager = SplitOutputManager().apply { this.managers.addAll(channels) }
    }
}

/**
 * An [OutputManager] that supports multiple outputs simultaneously
 */
class SplitOutputManager(val managers: MutableSet<OutputManager> = HashSet(), meta: Meta = Meta.empty()) : OutputManager, BasicPlugin(meta) {

    override fun get(meta: Meta): Output = splitOutput(*managers.map { it.get(meta) }.toTypedArray())

    override fun attach(context: Context) {
        super.attach(context)
        managers.forEach {
            context.pluginManager.loadDependencies(it)
            it.attach(context)
        }
    }

    override fun detach() {
        super.detach()
        managers.forEach { it.detach() }
    }

    companion object {
        /**
         * Convenience method to build split output manager
         */
        fun build(vararg managers: OutputManager): SplitOutputManager {
            return SplitOutputManager(hashSetOf(*managers))
        }
    }
}

operator fun OutputManager.plus(other: OutputManager): OutputManager {
    return when (this) {
        is SplitOutputManager -> {
            managers.add(other)// add to current output managers
            this
        }
        else -> {
            val split = SplitOutputManager.build(this, other)
            context.pluginManager.remove(this)
            context.pluginManager.load(split)
            split
        }
    }
}


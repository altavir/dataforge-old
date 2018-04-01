/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots

import hep.dataforge.context.Plugin
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.meta.Meta

/**
 * Любой объект, который может содержать график или набор графиков
 *
 * @author Alexander Nozik
 */
interface PlotPlugin : Plugin {

    /**
     * Get or create a plot frame with default meta
     *
     * @param name
     * @return
     * @throws NameNotFoundException
     */
    fun getPlotFrame(stage: String, name: String): PlotFrame

    fun getPlotFrame(stage: String, name: String, meta: Meta): PlotFrame {
        val frame = getPlotFrame(stage, name)
        frame.configure(meta)
        return frame
    }

    fun getPlotFrame(name: String, meta: Meta): PlotFrame {
        return getPlotFrame(DEFAULT_STAGE_NAME, name, meta)
    }


    fun getPlotFrame(name: String): PlotFrame {
        return getPlotFrame(DEFAULT_STAGE_NAME, name)
    }


    fun hasPlotFrame(stage: String, name: String): Boolean

    fun hasPlotFrame(name: String): Boolean {
        return hasPlotFrame(DEFAULT_STAGE_NAME, name)
    }

    companion object {
        val DEFAULT_STAGE_NAME = "default"
    }
}

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
package hep.dataforge.plots;

import hep.dataforge.context.Plugin;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;

/**
 * Любой объект, который может содержать график или набор графиков
 *
 * @author Alexander Nozik
 */
public interface PlotPlugin extends Plugin {
    String DEFAULT_STAGE_NAME = "default";

    /**
     * Get or create a plot frame with default meta
     *
     * @param name
     * @return
     * @throws NameNotFoundException
     */
    PlotFrame getPlotFrame(String stage, String name);

    default PlotFrame getPlotFrame(String stage, String name, Meta meta) {
        PlotFrame frame = getPlotFrame(stage, name);
        frame.configure(meta);
        return frame;
    }

    default PlotFrame getPlotFrame(String name, Meta meta) {
        return getPlotFrame(DEFAULT_STAGE_NAME, name, meta);
    }


    default PlotFrame getPlotFrame(String name) {
        return getPlotFrame(DEFAULT_STAGE_NAME, name);
    }


    boolean hasPlotFrame(String stage, String name);

    default boolean hasPlotFrame(String name) {
        return hasPlotFrame(DEFAULT_STAGE_NAME, name);
    }
}

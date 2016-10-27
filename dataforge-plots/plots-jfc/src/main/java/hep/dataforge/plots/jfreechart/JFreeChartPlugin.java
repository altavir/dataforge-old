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

package hep.dataforge.plots.jfreechart;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.plots.DefaultPlotHolder;
import hep.dataforge.plots.PlotsPlugin;

/**
 * Created by darksnake on 04-Sep-16.
 */
@PluginDef(name = "plots-jfc", group = "hep.dataforge", description = "JFreeChart plot library", dependsOn = {"hep.dataforge:plots"})
public class JFreeChartPlugin extends BasicPlugin {
    @Override
    public void attach(Context context) {
        super.attach(context);
        context.provide("plots", PlotsPlugin.class)
                .setPlotHolderDelegate(new DefaultPlotHolder(() -> new JFreeChartFrame()));
    }

    @Override
    public void detach() {
        if (getContext() != null) {
            getContext().provide("plots", PlotsPlugin.class).setPlotHolderDelegate(new DefaultPlotHolder());
        }
        super.detach();
    }
}

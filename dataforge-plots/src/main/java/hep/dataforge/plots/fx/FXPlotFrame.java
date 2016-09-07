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

package hep.dataforge.plots.fx;

import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.Plottable;
import javafx.scene.layout.AnchorPane;

/**
 * Created by darksnake on 04-Sep-16.
 */
public interface FXPlotFrame<T extends Plottable> extends PlotFrame<T> {
    /**
     * Display plot in JavaFx container
     *
     * @param container
     */
    void display(AnchorPane container);
}
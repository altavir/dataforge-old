package hep.dataforge.kodex.fx.plots

import hep.dataforge.plots.PlotGroup
import hep.dataforge.plots.Plottable

operator fun PlotGroup.plus(pl: Plottable){
    this.add(pl);
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;

/**
 * A plugin for displaying plots. It works as a PlotHolder but delegates all
 * methods to internal delegate.
 *
 s* @author Alexander Nozik
 */
@PluginDef(name = "plots", group = "hep.dataforge", description = "Basic plottiong plugin")
public class PlotsPlugin extends BasicPlugin implements PlotHolder {
    
    public static PlotsPlugin buildFrom(Context context){
        PlotsPlugin plugin = context.provide("plots", PlotsPlugin.class);
        return plugin;
    }

    PlotHolder plotHolderDelegate = new DefaultPlotHolder();

    @Override
    public void attach(Context context) {

    }

    @Override
    public void detach(Context context) {

    }

    public void setPlotHolderDelegate(PlotHolder holderDelegate) {
        this.plotHolderDelegate = holderDelegate;
    }

    @Override
    public PlotFrame buildPlotFrame(String stage, String name, Meta annotation) {
        return plotHolderDelegate.buildPlotFrame(stage, name, annotation);
    }

    @Override
    public PlotFrame getPlotFrame(String stage, String name) throws NameNotFoundException {
        return plotHolderDelegate.getPlotFrame(stage, name);
    }

    @Override
    public boolean hasPlotFrame(String stage, String name) {
        return plotHolderDelegate.hasPlotFrame(stage, name);
    }

}

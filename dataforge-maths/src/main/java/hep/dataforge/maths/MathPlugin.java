/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.maths.functions.FunctionDispatcher;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.MetaFactory;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "math", group = "hep.dataforge", description = "General mathematics plugin contining function factories")
public class MathPlugin extends BasicPlugin {

    public static MathPlugin buildFrom(Context context) {
        MathPlugin plugin = context.provide("math", MathPlugin.class);
        return plugin;
    }

    private final FunctionDispatcher<UnivariateFunction> univariateFactory = new FunctionDispatcher<>();
    private final FunctionDispatcher<BivariateFunction> bivariateFactory = new FunctionDispatcher<>();

    public UnivariateFunction buildUnivariateFunction(Meta meta) {
        return univariateFactory.build(meta);
    }

    public void registerUnivariate(String type, MetaFactory<UnivariateFunction> factory) {
        this.univariateFactory.addFactory(type, factory);
    }

    public BivariateFunction buildBivariateFunction(Meta meta) {
        return bivariateFactory.build(meta);
    }

    public BivariateFunction buildBivariateFunction(String type) {
        return bivariateFactory.build(new MetaBuilder("").setValue("type", type));
    }

    public void registerBivariate(String type, MetaFactory<BivariateFunction> factory) {
        this.bivariateFactory.addFactory(type, factory);
    }

    public void registerBivariate(String type, BivariateFunction function) {
        this.bivariateFactory.addFactory(type, meta -> function);
    }

}

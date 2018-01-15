/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths;

import hep.dataforge.context.*;
import hep.dataforge.maths.functions.FunctionDispatcher;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.MetaFactory;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Mathematical plugin. Stores function library and other useful things.
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "math", group = "hep.dataforge", info = "General mathematics plugin contining function factories")
public class MathPlugin extends BasicPlugin {

    private final FunctionDispatcher<UnivariateFunction> univariateFactory = new FunctionDispatcher<>();
    private final FunctionDispatcher<BivariateFunction> bivariateFactory = new FunctionDispatcher<>();

    public static MathPlugin buildFrom(Context context) {
        return context.getPluginManager().getOrLoad(MathPlugin.class);
    }

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

    public static class Factory implements PluginFactory {

        @Override
        public PluginTag getTag() {
            return Plugin.resolveTag(MathPlugin.class);
        }

        @Override
        public Plugin build(Meta meta) {
            return new MathPlugin();
        }
    }

}

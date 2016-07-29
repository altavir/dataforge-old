/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.maths.functions.UnivariateFunctionDispatcher;
import hep.dataforge.meta.Meta;
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

    private final UnivariateFunctionDispatcher univariateFactory = new UnivariateFunctionDispatcher();

    public UnivariateFunction buildUnivariateFunction(Meta meta) {
        return univariateFactory.build(meta);
    }
}

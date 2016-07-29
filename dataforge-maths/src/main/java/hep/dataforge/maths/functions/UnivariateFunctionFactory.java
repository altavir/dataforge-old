/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.functions;

import hep.dataforge.meta.Meta;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * A factory to produce univariate functions. Functions produced by factory
 * should be as static as possible to maintain performance.
 *
 * @author Alexander Nozik
 */
public interface UnivariateFunctionFactory {
    UnivariateFunction build(Meta meta);
}

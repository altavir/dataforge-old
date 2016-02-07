/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

/**
 * Parametric factory for named objects
 *
 * @author Alexander Nozik
 */
public interface NamedMetaFactory<T> {

    T build(String name, Context context, Meta annotation);

    default T build(String name) {
        return build(name, GlobalContext.instance(), MetaBuilder.buildEmpty(null));
    }
}

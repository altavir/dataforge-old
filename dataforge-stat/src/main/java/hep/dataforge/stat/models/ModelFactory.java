/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.stat.models;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.utils.ContextMetaFactory;

/**
 * A factory
 *
 * @author Alexander Nozik
 */
public interface ModelFactory extends ContextMetaFactory<Model>, Named {
    static ModelFactory build(String name, ModelDescriptor descriptor, ContextMetaFactory<Model> factory) {
        return new ModelFactory() {
            @Override
            public ModelDescriptor getDescriptor() {
                return descriptor;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Model build(Context context, Meta meta) {
                return factory.build(context, meta);
            }
        };
    }

    ModelDescriptor getDescriptor();
}
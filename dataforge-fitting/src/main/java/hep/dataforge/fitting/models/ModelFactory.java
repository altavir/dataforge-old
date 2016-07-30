/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fitting.models;

import hep.dataforge.utils.ContextMetaFactory;

/**
 *
 * @author Alexander Nozik
 */
@FunctionalInterface
public interface ModelFactory extends ContextMetaFactory<Model> {
    default ModelDescriptor descriptor(){
        return null;
    }
}

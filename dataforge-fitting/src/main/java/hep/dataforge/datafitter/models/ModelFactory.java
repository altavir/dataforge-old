/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.datafitter.models;

import hep.dataforge.utils.MetaFactory;

/**
 *
 * @author Alexander Nozik
 */
@FunctionalInterface
public interface ModelFactory extends MetaFactory<Model> {
    default ModelDescriptor descriptor(){
        return null;
    }
}

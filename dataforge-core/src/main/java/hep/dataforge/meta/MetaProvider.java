/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.NameNotFoundException;

/**
 *
 * @author Alexander Nozik
 */
public interface MetaProvider {

    default boolean hasMeta(String path) {
        try {
            return getMeta(path) != null;
        } catch (NameNotFoundException ex) {
            return false;
        }
    }

    Meta getMeta(String path);
}

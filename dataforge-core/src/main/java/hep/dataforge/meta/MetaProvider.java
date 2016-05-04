/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.navigation.Path;
import hep.dataforge.navigation.Provider;

/**
 *
 * @author Alexander Nozik
 */
public interface MetaProvider {

    public static final String META_TARGET = "meta";

    /**
     * Build a meta provider from given general provider
     *
     * @param provider
     * @return
     */
    public static MetaProvider buildFrom(Provider provider) {
        if(provider instanceof MetaProvider){
            return (MetaProvider) provider;
        }
        return new MetaProvider() {
            @Override
            public Meta getMeta(String path) {
                return provider.provide(Path.of(path, META_TARGET), Meta.class);
            }

            @Override
            public boolean hasMeta(String path) {
                return provider.provides(Path.of(path, META_TARGET));
            }

        };
    }

    default boolean hasMeta(String path) {
        try {
            return getMeta(path) != null;
        } catch (NameNotFoundException ex) {
            return false;
        }
    }

    Meta getMeta(String path);
//
//    @Override
//    public default Value getValue(String path) {
//        Name pathName = Name.of(path);
//        String metaPath = pathName.cutLast().toString();
//        if(hasMeta(metaPath)){
//            return getMeta(metaPath).getValue(pathName.getLast().toString());
//        } else {
//            return null;
//        }
//    }
//    

}

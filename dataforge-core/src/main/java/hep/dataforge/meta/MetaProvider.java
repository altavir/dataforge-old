/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.providers.Path;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Alexander Nozik
 */
public interface MetaProvider {

    String META_TARGET = "meta";

    /**
     * Build a meta provider from given general provider
     *
     * @param provider
     * @return
     */
    static MetaProvider buildFrom(Provider provider) {
        if (provider instanceof MetaProvider) {
            return (MetaProvider) provider;
        }
        return path -> provider.provide(Path.of(path, META_TARGET)).map(it -> Meta.class.cast(it));
    }


//    default boolean hasMeta(String path) {
//        return optMeta(path).isPresent();
//    }

    @Provides(META_TARGET)
    Optional<? extends Meta> optMeta(String path);

    default Meta getMeta(String path) {
        return optMeta(path).orElseThrow(() -> new NameNotFoundException(path));
    }

    /**
     * Return a child node with given name or default if child node not found
     *
     * @param path
     * @param def
     * @return
     */
    default Meta getMeta(String path, Meta def) {
        return optMeta(path).<Meta>map(it -> it).orElse(def);
    }

    default Meta getMeta(String path, Supplier<Meta> def) {
        return optMeta(path).<Meta>map(it -> it).orElseGet(def);
    }

    default boolean hasMeta(String path) {
        return optMeta(path).isPresent();
    }

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

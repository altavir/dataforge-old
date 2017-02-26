/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 *
 * @author Alexander Nozik
 */
public interface MetaType {

    ServiceLoader<MetaType> loader = ServiceLoader.load(MetaType.class);

    /**
     * Resolve a meta type code and return null if code could not be resolved
     * @param code
     * @return
     */
    static MetaType resolve(short code){
        return StreamSupport.stream(loader.spliterator(),false)
                .filter(it-> it.getCode() == code).findFirst().orElse(null);
    }

    /**
     * Resolve a meta type and return null if it could not be resolved
     * @param name
     * @return
     */
    static MetaType resolve(String name){
        return StreamSupport.stream(loader.spliterator(),false)
                .filter(it-> Objects.equals(it.getName(), name)).findFirst().orElse(null);
    }

    short getCode();

    String getName();

    MetaStreamReader getReader();

    MetaStreamWriter getWriter();

    /**
     * A file name filter for meta encoded in this format
     * @return
     */
    Predicate<String> fileNameFilter();
}

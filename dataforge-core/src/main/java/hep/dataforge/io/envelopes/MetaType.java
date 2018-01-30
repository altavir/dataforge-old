/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.context.Global;
import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static hep.dataforge.io.envelopes.Envelope.META_TYPE_KEY;

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
        //TODO add caching here?
        synchronized (Global.Companion.instance()) {
            return StreamSupport.stream(loader.spliterator(), false)
                    .filter(it -> it.getCodes().contains(code)).findFirst().orElse(null);
        }
    }

    /**
     * Resolve a meta type and return null if it could not be resolved
     * @param name
     * @return
     */
    static MetaType resolve(String name){
        synchronized (Global.Companion.instance()) {
            return StreamSupport.stream(loader.spliterator(), false)
                    .filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }

    static MetaType resolve(Map<String, String> properties) {
        if (properties.containsKey(META_TYPE_KEY)) {
            return MetaType.resolve(properties.get(META_TYPE_KEY));
        } else {
            return XMLMetaType.instance;
        }
    }


    List<Short> getCodes();

    String getName();

    MetaStreamReader getReader();

    MetaStreamWriter getWriter();

    /**
     * A file name filter for meta encoded in this format
     * @return
     */
    Predicate<String> fileNameFilter();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class governing loading and creating of contexts
 *
 * @author Alexander Nozik
 */
public class ContextLoader {

    private static final ContextLoader instance = new ContextLoader();

    public static ContextLoader instance() {
        return instance;
    }

    private final Map<String, MetaFactory<Context>> builders = new HashMap<>();

    public ContextLoader() {
    }

    /**
     * Build new context using given name as builder reference. Given name is
     * not necessarily is the name of resulting context.
     *
     * @param name
     * @param parent - parent for newly built context. If null, than Global
     * context is ised as a parent
     * @param annotation
     * @return
     */
    public Context buildContext(String name, Context parent, Meta annotation) {
        if ("df".equals(name) || "global".equals("name")) {
            return GlobalContext.instance();
        } else {
            if (builders.containsKey(name)) {
                return builders.get(name).build(parent, annotation);
            } else {
                throw new NameNotFoundException(name, "No named context builder found with the given name");
            }
        }
    }

    /**
     * Registering new context factory under reserved names like "df" or
     * "global" does nothing. Registering factory which already exists replaces
     * it.
     *
     * @param name
     * @param factory
     */
    public void registerContextFactory(String name, MetaFactory<Context> factory) {
        builders.put(name, factory);
    }

}

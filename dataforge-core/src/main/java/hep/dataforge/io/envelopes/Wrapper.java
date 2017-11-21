/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.names.Named;

/**
 * The class to unwrap object of specific type from envelope. Generally, T is
 * supposed to be Wrappable, but it is not guaranteed.
 *
 * @author Alexander Nozik
 */
public interface Wrapper<T> extends Named {
    String WRAPPER_TYPE_KEY = "@wrapper.type";
    String WRAPPER_CLASS_KEY = "@wrapper.class";

    @SuppressWarnings("unchecked")
    static <T> T unwrap(Context context, Envelope envelope) throws Exception {
        Wrapper<T> wrapper;
        if (envelope.getMeta().hasValue(WRAPPER_CLASS_KEY)) {
            wrapper = (Wrapper<T>) Class.forName(envelope.getMeta().getString(WRAPPER_CLASS_KEY)).getConstructor().newInstance();
        } else if (envelope.getMeta().hasValue(WRAPPER_TYPE_KEY)) {
            wrapper = (Wrapper<T>) context.findService(Wrapper.class, it -> it.getName().equals(envelope.getMeta().getString(WRAPPER_TYPE_KEY)))
                    .orElseThrow(() -> new RuntimeException("Unwrapper not found"));
        } else {
            throw new IllegalArgumentException("Not a wrapper envelope");
        }
        return wrapper.unWrap(envelope);
    }

    static <T> T unwrap(Envelope envelope) throws Exception {
        return unwrap(Global.instance(), envelope);
    }

    @SuppressWarnings("unchecked")
    static Envelope wrap(Context context, Object object) {
        Wrapper<Object> wrapper = context
                .findService(Wrapper.class, it -> it.getType() != Object.class && it.getType().isInstance(object))
                .orElse(new JavaObjectWrapper());        //TODO check for Serializable
        return wrapper.wrap(object);
    }

    Class<T> getType();

    Envelope wrap(T object);

    T unWrap(Envelope envelope);

    default void checkValidEnvelope(Envelope env) {
        if (!env.getMeta().getString(WRAPPER_TYPE_KEY, "").equals(getName())) {
            throw new RuntimeException("Can't unwrap envelope. Wrong content type.");
        }
    }
}

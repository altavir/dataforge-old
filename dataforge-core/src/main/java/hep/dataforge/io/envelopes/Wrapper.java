/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.context.Context;
import hep.dataforge.names.Named;

/**
 * The class to unwrap object of specific type from envelope. Generally, T is
 * supposed to be Wrappable, but it is not guaranteed.
 *
 * @author Alexander Nozik
 */
public interface Wrapper<T> extends Named {
    String WRAPPER_KEY = "wrapper";

    @SuppressWarnings("unchecked")
    static <T> T unwrap(Context context, Envelope envelope) {
        Wrapper<T> wrapper = context
                .findService(Wrapper.class, it -> it.getName().equals(envelope.meta().getString(WRAPPER_KEY)))
                .orElseThrow(() -> new RuntimeException("Unwrapper not found"));
        return wrapper.unWrap(envelope);
    }

    @SuppressWarnings("unchecked")
    static Envelope wrap(Context context, Object object) {
        Wrapper<Object> wrapper = context
                .findService(Wrapper.class, it -> it.getType() != Object.class && it.getType().isInstance(object))
                .orElse(new JavaObjectWrapper());
        return wrapper.wrap(object);
    }

    Class<T> getType();

    Envelope wrap(T object);

    T unWrap(Envelope envelope);

    default void checkValidEnvelope(Envelope env) {
        if (!env.meta().getString(WRAPPER_KEY, "").equals(getName())) {
            throw new RuntimeException("Can't unwrap envelope. Wrong content type.");
        }
    }
}

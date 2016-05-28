/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import static hep.dataforge.io.envelopes.Wrappable.WRAPPED_TYPE_KEY;

/**
 * The class to unwrap object of specific type from envelope. Generally, T is
 * supposed to be Wrappable, but it is not guaranteed.
 *
 * @author Alexander Nozik
 */
public interface UnWrapper<T> {

    /**
     * Type frome envelope meta 'type' field
     *
     * @return
     */
    String type();

    T unWrap(Envelope envelope);

    default boolean isValidEnvelope(Envelope env) {
        return env.meta().getString(WRAPPED_TYPE_KEY, "").equals(type());
    }
}

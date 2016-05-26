/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

/**
 * The class to unwrap object of specific type from envelope. Generally, T is
 * supposed to be Wrappable, but it is not guaranteed.
 *
 * @author Alexander Nozik
 */
public interface UnWrapper<T> {

    /**
     * Type frome envelope meta 'type' field
     * @return 
     */
    String type();

    T unWrap(Envelope envelope);
}

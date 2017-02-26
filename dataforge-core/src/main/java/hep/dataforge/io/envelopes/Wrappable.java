/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

/**
 * Represent an object that can wrap itself into envelope
 *
 * @author Alexander Nozik
 */
public interface Wrappable {

    int WRAPPER_ENVELOPE_CODE = 0x4446;//DF
    String WRAPPED_TYPE_KEY = "type";

    Envelope wrap();

    //PENDING add unwrapper class directly to wrapped envelope?
}

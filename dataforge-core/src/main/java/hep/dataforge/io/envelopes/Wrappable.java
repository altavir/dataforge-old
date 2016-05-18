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

    public static final short DEFAULT_WRAPPER_ENVELOPE_CODE = 0x5752;//WR

    Envelope wrap();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.io.messages.Dispatcher;
import hep.dataforge.io.messages.Responder;

/**
 * A Responder that does not respond itself but delegates response to
 * appropriate responder.
 *
 * @author Alexander Nozik
 */
public interface ResponseDispatcher extends Responder, Dispatcher {

    @Override
    public default Envelope respond(Envelope message) {
        return getResponder(message).respond(message);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.messages;

import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;

/**
 * A dispatcher of messages that could provide appropriate responder for
 * message. The dispatcher does not handle message itself
 *
 * @author Alexander Nozik
 */
public interface Dispatcher {
    String MESSAGE_TARGET_NODE = "@target";
    String TARGET_TYPE_KEY = "type";
    String TARGET_NAME_KEY = "name";

    @ValueDef(name = TARGET_TYPE_KEY, info = "The type of the message target")
    @ValueDef(name = TARGET_NAME_KEY, info = "The name of the message target")
    Responder getResponder(Meta targetInfo) throws EnvelopeTargetNotFoundException;
    
    default Responder getResponder(Envelope envelope){
        return getResponder(envelope.getMeta().getMeta(MESSAGE_TARGET_NODE));
    }
}

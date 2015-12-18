/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.exceptions.EnvelopeTargetNotFoundException;
import hep.dataforge.meta.Meta;

/**
 * A dispatcher of messages that could provide appropriate responder for
 * message. The dispatcher does not handle message itself
 *
 * @author Alexander Nozik
 */
public interface Dispatcher {
    public static final String ENVELOPE_META_TARGET_NODE = "target";
    public static final String TARGET_NAME_KEY = "name";
    public static final String TARGET_TYPE_KEY = "type";
   
    Responder getResponder(Meta targetInfo) throws EnvelopeTargetNotFoundException;
    
    default Responder getResponder(Envelope envelope){
        return getResponder(envelope.meta().getNode(ENVELOPE_META_TARGET_NODE));
    }
}

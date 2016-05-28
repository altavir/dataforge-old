/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;

/**
 * A messaging target or source
 *
 * @author Alexander Nozik
 */
public interface Destination {
    //TODO remove or revise
    public static final String TARGET_NAME_KEY = "name";
    public static final String TARGET_TYPE_KEY = "type";

    public static final String ENVELOPE_DESTINATION_NODE = "destination";

    /**
     * Get a target meta designation for this target
     *
     * @return
     */
    @ValueDef(name = "type")
    @ValueDef(name = "name")
    public Meta destinationMeta();

    /**
     * Check if this target for given envelope. By default envelope is accepted
     * if target information in the envelope meta is missing or if target meta
     * equals the one provided by {@code destinationMeta} method.
     *
     * @param env
     * @return
     */
    default boolean acceptEnvelope(Envelope env) {
        return !env.meta().hasNode(ENVELOPE_DESTINATION_NODE)
                || env.meta().getNode(ENVELOPE_DESTINATION_NODE).equals(destinationMeta());
    }
}

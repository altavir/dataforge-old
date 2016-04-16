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
public interface Target {

    public static final String TARGET_NAME_KEY = "name";
    public static final String TARGET_TYPE_KEY = "type";

    public static final String ENVELOPE_TARGET_NODE = "target";

    /**
     * Get a target meta designation for this target
     *
     * @return
     */
    @ValueDef(name = "type")
    @ValueDef(name = "name")
    public Meta targetDescription();

    /**
     * Check if this target for given envelope. By default envelope is accepted
     * if target information in the envelope meta is missing or if target meta
     * equals the one provided by {@code targetDescription} method.
     *
     * @param env
     * @return
     */
    default boolean acceptEnvelope(Envelope env) {
        return !env.meta().hasNode(ENVELOPE_TARGET_NODE)
                || env.meta().getNode(ENVELOPE_TARGET_NODE).equals(targetDescription());
    }
}

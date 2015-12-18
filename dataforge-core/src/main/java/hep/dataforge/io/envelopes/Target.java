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

    /**
     * Get a target meta designation for this target
     *
     * @return
     */
    @ValueDef(name = "type")
    @ValueDef(name = "name")
    public Meta getTargetMeta();
}

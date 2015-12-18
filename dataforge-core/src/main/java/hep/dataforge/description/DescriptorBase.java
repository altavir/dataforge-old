/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import java.io.Serializable;

/**
 * The base class for descriptors
 * @author Alexander Nozik
 */
public class DescriptorBase implements Annotated, Serializable{

    private final Meta meta;

    protected DescriptorBase(String name) {
        this.meta = Meta.buildEmpty(name);
    }

    protected DescriptorBase(Meta meta) {
        this.meta = meta;
    }

    @Override
    public Meta meta() {
        if (meta == null) {
            return Meta.buildEmpty("descriptor");
        } else {
            return meta;
        }
    }

}

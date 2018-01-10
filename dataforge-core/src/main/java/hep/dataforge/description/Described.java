/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.io.markup.MarkupBuilder;
import hep.dataforge.names.Named;
import org.jetbrains.annotations.Nullable;

/**
 * A general interface for something with meta description
 *
 * @author Alexander Nozik
 */
public interface Described {
    /**
     * Provide a description
     *
     * @return
     */
    default NodeDescriptor getDescriptor() {
        return DescriptorUtils.buildDescriptor(getClass());
    }

    /**
     * Header markup. Null corresponds to no header
     *
     * @return
     */
    @Nullable
    default MarkupBuilder getHeader() {
        if (this instanceof Named) {
            return new MarkupBuilder().text(((Named) this).getName(), "blue");
        } else {
            //TODO add customizable markup for different renderers
            return new MarkupBuilder();
        }
    }
}

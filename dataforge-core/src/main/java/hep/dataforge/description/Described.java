/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.Named;
import hep.dataforge.io.markup.Markup;
import hep.dataforge.io.markup.TextMarkup;

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
        return Descriptors.buildDescriptor(getClass());
    }

    /**
     * Header markup. Null corresponds to no header
     *
     * @return
     */
    default Markup getHeader() {
        if (this instanceof Named) {
            TextMarkup res = new TextMarkup();
            res.setColor("blue");
            return res;// new MarkupBuilder().text(((Named) this).getName(), "blue");
        } else {
            //TODO add customizable markup for different renderers
            return new TextMarkup();
        }
    }
}

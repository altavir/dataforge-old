/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

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
}

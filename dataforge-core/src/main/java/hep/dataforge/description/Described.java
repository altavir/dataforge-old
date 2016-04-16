/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

/**
 *
 * @author Alexander Nozik
 */
public interface Described {
    default NodeDescriptor getDescriptor(){
        return DescriptorUtils.buildDescriptor(getClass());
    }
}

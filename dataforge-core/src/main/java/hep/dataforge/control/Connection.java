/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control;

/**
 * A connection which could be applied to object that could receive connection.
 * Usually connection does not invoke {@code open} method itself, but delegates it to {@code Connectible}
 *
 * @author Alexander Nozik
 */
public interface Connection extends AutoCloseable {
    default boolean isOpen(){
        return true;
    }

    default void open(Object object) throws Exception{
        //do nothing
    }

    @Override
    default void close() throws Exception {
        //do nothing
    }
}

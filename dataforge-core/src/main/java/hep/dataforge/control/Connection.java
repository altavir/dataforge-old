/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control;

/**
 * A connection which could be applied to object that could receive connection.
 * Usually connection does not invoke {@code open} method itself, but delegates it to {@code Connectable}
 *
 * @author Alexander Nozik
 */
public interface Connection<T> extends AutoCloseable {
    boolean isOpen();

    void open(T object) throws Exception;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

/**
 *
 * @author Alexander Nozik
 */
public interface Connectable<C extends Connection<T>,T> {
    C connectTo(T target);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

/**
 * The connection between devices or other control objects
 * @author Alexander Nozik
 */
public interface Connection extends AutoCloseable{
    String type();
    
    boolean isOpen();
    
    void open() throws Exception;
}

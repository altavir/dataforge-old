/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.content.Named;
import hep.dataforge.io.envelopes.Responder;

/**
 * The connection between devices or other control objects
 * @author Alexander Nozik
 */
public interface Connection extends Named, Responder, AutoCloseable{
    boolean isOpen();
    
    void open() throws Exception;
}

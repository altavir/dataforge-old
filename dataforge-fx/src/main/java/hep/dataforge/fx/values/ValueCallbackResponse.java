/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.values.Value;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class ValueCallbackResponse {
    /**
     * Set value success
     */
    boolean success;
    /**
     *  Value after change
     */
    Value value;
    /**
     * Message on unsuccessful change
     */
    String message;

    public ValueCallbackResponse(boolean success, Value value, String message) {
        this.success = success;
        this.value = value;
        this.message = message;
    }
    
    
}

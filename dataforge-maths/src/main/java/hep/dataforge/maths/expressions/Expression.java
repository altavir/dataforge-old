/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.expressions;

import hep.dataforge.values.Value;

/**
 * The result of expression evaluation
 *
 * @author Alexander Nozik
 */
public interface Expression {
    /**
     * The number of unresolved parameters
     * @return 
     */
    int parNum();
    
    /**
     * The number of variables
     * @return 
     */
    int varNum();
    
    /**
     * Evaluate t
     * @return 
     */
    Value value();
}

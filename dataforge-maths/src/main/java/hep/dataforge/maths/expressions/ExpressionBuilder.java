/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.maths.expressions;

import hep.dataforge.values.Value;

/**
 *
 * @author Alexander Nozik
 */
public interface ExpressionBuilder {
    
    /**
     * Set expression string
     * @param expression
     * @return 
     */
    ExpressionBuilder expr(String expression);
    
    /**
     * Set parameter value
     * @param parName
     * @param par
     * @return 
     */
    ExpressionBuilder par(String parName, Object par);
    
    /**
     * Designate a variable
     * @param varName
     * @return 
     */
    ExpressionBuilder var(String varName);
    
    /**
     * Evaluate expression using provided 
     * @return 
     */
    Expression eval();
}

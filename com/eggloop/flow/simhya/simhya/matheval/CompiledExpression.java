/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matheval;

/**
 *
 * @author Luca
 */
public interface CompiledExpression {
    
    /**
     * Sets the reference to global variables
     * @param vars 
     */
    public void setVariableReference(double[] vars);
    /**
     * Sets the reference to global constants
     * @param params 
     */
    public void setParameterReference(double[] params);
    /**
     * Evaluates the compiled expression w.r.t. current state of the store
     * @return 
     */
    public double evaluate();
    /**
     * evaluate the expression using passed variables instead of global ones
     * @param vars
     * @return 
     */
    public double evaluate(SymbolArray vars);
    /**
     * evaluate the expression using passed variables instead of global ones
     * @param vars
     * @return 
     */
    public double evaluate(double[] vars);
    
    /**
     * Evaluates a logical expression returning a boolean value.
     * @return 
     * @throws EvalException is the expression is a numerical expression.
     */
    public boolean evaluateBool();
    /**
     * Evaluates a logical expression returning a boolean value using passed variables instead of global ones.
     * @return 
     * @throws EvalException is the expression is a numerical expression.
     */
    public boolean evaluateBool(SymbolArray vars);
    /**
     * Evaluates a logical expression returning a boolean value using passed variables instead of global ones.
     * @return 
     * @throws EvalException is the expression is a numerical expression.
     */
    public boolean evaluateBool(double[] vars);
    /**
     * returns true if the compiled expression is a boolean formula.
     * @return 
     */
    public boolean isLogical();
}

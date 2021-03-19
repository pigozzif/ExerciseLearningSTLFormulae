/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store;

import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import org.sbml.jsbml.ASTNode;

import java.util.ArrayList;


/**
 * @author Luca
 */
public interface Function {

    /**
     * Computes the  function, according to the current state of the store
     *
     * @return the current value of the function
     */
    public double compute();

    /**
     * computes the value of the function, according to a given value of variables.
     *
     * @param vars the value of variables, as a double array.
     * @return the computed value of the function
     */
    public double compute(SymbolArray vars);

    /**
     * Computes the  function, according to the current state of the store
     * using a chace mechanism to avoid recomputing the same expression twice
     * To be used if expressions share many subterms.
     * Recomputation can be forced by changing the evaluation round in the store.
     *
     * @return the current value of the function
     */
    public double computeCache();

    /**
     * computes the value of the function, according to a given value of variables,
     * using a chace mechanism to avoid recomputing the same expression twice
     * To be used if expressions share many subterms.
     * Recomputation can be forced by changing the evaluation round in the store.
     *
     * @param vars the value of variables, as a double array.
     * @return the computed value of the function
     */
    public double computeCache(SymbolArray vars);

    /**
     * @return a list of variable ID of the variables involved in the function.
     */
    public ArrayList<Integer> getVariableList();

    /**
     * completes the initialization of the function, if needed;
     */
    public void initialize();

    /**
     * @return the model language representation of the function
     */
    public String toModelLanguage();

    /**
     * @return true if the function is a constant increment function
     */
    public boolean isConstantIncrementFunction();


    /**
     * @return the constant increment if the function is a constant increment function
     * throws an exception otherwise.
     */
    public double getConstantIncrement();


    /**
     * Substitutes variables in the function  with passed expressions
     * For special predicates, a check on the expression type is carried out,
     * and if expression is not in the right form, an exception is thrown.
     *
     * @param varNames
     * @param expressions
     * @return
     */
    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions);

    /**
     * converts the function to a MathML representation, using
     * JSBML library
     *
     * @return an ASTNode
     */
    public ASTNode convertToJSBML();


    /**
     * Clones the function with respect to a new Store, adding it to the new store,
     *
     * @param newStore
     * @return
     */
    public Function clone(Store newStore);

    /**
     * Return true if the function is the constant zero
     *
     * @return
     */
    public boolean isConstantZero();

    /**
     * Differentiates the function w.r.t. symbol x
     *
     * @param x a symbol
     * @return the derivative function.
     */
    public Function differentiate(String x);

    /**
     * Converts the function to a matlab expression
     *
     * @return
     */
    public String toMatlabCode();

}

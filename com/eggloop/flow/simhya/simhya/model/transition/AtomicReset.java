/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.transition;

import  com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import  com.eggloop.flow.simhya.simhya.model.store.Store;
import java.util.ArrayList;
import  com.eggloop.flow.simhya.simhya.matheval.Expression;
import org.sbml.jsbml.ASTNode;

/**
 *
 * @author Luca
 */
public interface AtomicReset {

    /**
     * computes the new values of the variable(s) affected by this atomic reset,
     * and stores them into temporary variables. New values are computed according
     * to the current state of the store, or the current state of variables
     * known by the object.
     */
    public void computeNewValues();
        /**
     * computes the new values of the variable(s) affected by this atomic reset,
     * using the cache mechanism of the store,
     * and stores them into temporary variables. New values are computed according
     * to the current state of the store, or the current state of variables
     * known by the object.
     */
    public void computeNewValuesCache();
    /**
    * computes the new values of the variable(s) affected by this atomic reset,
     * and stores them into temporary variables. new values are computed according
     * to the passed values.
     * @param variables a simbolarray storing values according to which new values
     * are to be comnputed
     */
    public void computeNewValues(SymbolArray variables);
    /**
     * computes the new values of the variable(s) affected by this atomic reset,
     * using a caching mechanism (warning -> change evaluation code if you change
     * variables)
     * and stores them into temporary variables. new values are computed according
     * to the passed values.
     * @param variables a simbolarray storing values according to which new values
     * are to be comnputed
     */
    public void computeNewValuesCache(SymbolArray variables);

    /**
     * Updates passed variable according to the computed new values.
     * WARNING: no control is made on the order with which {@link #computeNewValues() }
     * and this function are called. Logical errors may occur if
     * {@link #computeNewValues() } is not called before.
     * @param variables the variables to be updated
     */
    public void updateStoreVariables(double[] variables);
    /**
     * Updates passed variable according to the computed new values.
     * WARNING: no control is made on the order with which {@link #computeNewValues() }
     * and this function are called. Logical errors may occur if
     * {@link #computeNewValues() } is not called before.
     * @param variables the variables to be updated
     */
    public void updateStoreVariables(SymbolArray variables);

    /**
     * returns true if the atomic reset is a constant increment, i.e. increases
     * a single variable by a constant.
     * @return
     */
    public boolean isConstantIncrement();

    /**
     * returns the constatn increment for the updated variable, IF
     * the atomic reset is of the constant increment type.
     * @return
     */
    public double getConstantIncrement();

    /**
     * returns a list with the IDs of updated variables.
     * @return
     */
    public ArrayList<Integer> getUpdatedVariables();

    /**
     * checks if the atomic reset is a basic reset for the given variable
     * @param variable
     * @return
     */
    public boolean isSimpleResetForVariable(int variable);

    public String toModelLanguage(Store store);

    /**
     * Substitutes variables in the current atomic reset. If the variable updated by the reset has
     * to be substituted, and it is to be replaced by an expression which is not a global variable,
     * an error will be raised.
     * @param varNames
     * @param expressions
     * @return
     */
    public AtomicReset substitute(ArrayList<String> varNames, ArrayList<Expression> expressions);

    /**
     * Converts the expression in the atomicReset assigned to the variable into
     * a MathML representation using JSBML
     * @return 
     */
    public ASTNode convertToJSBML();
    
    public String toMatlabCode();
    
    /*
     * TODO:
     * construct an interface for atomic resets,
     * supporting two methods:
     * computeNewValues
     * updateStore
     * Then do two implementations. The first is like the
     * current one: variable to reset + function
     * The second one wraps up a constraint objects
     *
     */
    
    
    /**
     * Returns a copy of the Atomic Reset registering its function ot the new store
     * @param newStore
     * @return 
     */
    AtomicReset clone(Store newStore);

}

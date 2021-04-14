/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.store;

import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import org.sbml.jsbml.ASTNode;

import java.util.ArrayList;



/**
 *
 * @author Luca
 */
public interface Predicate {
    
    /**
     * evaluates the truth value of the predicate, according to the current state of the store
     * @return the new truth value
     */
    public boolean evaluate();
    /**
     * evaluates the thruth value of the predicate, according to passed variables
     * @param vars the value of variables
     * @return the truth value of the predicate
     */
    public boolean evaluate(SymbolArray vars);
    /**
     * evaluates the thruth value of the predicate, according to the current state of the store
     * using a chace mechanism to avoid recomputing the same expression twice
     * To be used if expressions share many subterms.
     * Recomputation can be forced by changing the evaluation round in the store.
     * @return the current value of the function
     */
    public boolean evaluateCache();
    /**
     * evaluates the thruth value of the predicate, according to a given value of variables,
     * using a chace mechanism to avoid recomputing the same expression twice
     * To be used if expressions share many subterms.
     * Recomputation can be forced by changing the evaluation round in the store.
     * @param vars the value of variables, as a double array.
     * @return the computed value of the function
     */
    public boolean evaluateCache(SymbolArray vars);
    /**
     *
     * @return a list of variable ID of the variables involved in the function.
     */
    public ArrayList<Integer> getVariableList();

    /**
     * completes the initialization of the predicate, if needed;
     */
    public void initialize();


    public boolean isTautology();
    public boolean isContradiction();

    /**
     *
     * @return the model language representation of the predicate
     */
    public String toModelLanguage();

    /**
     * checks if the logical predicate can be converted into a function.
     * This requires that the predicate is defined only by inequalities.
     * @return 
     */
    public boolean canBeConvertedToFunction();

    /**
     * Converts the predicate to a function, throws an exception if it is
     * not possible to convert it.
     * @return a function which takes negative values when the predicate is 
     * false and positive values when it is true.
     * To be used with event detection facilities of ode integators.
     */
    public Function convertToFunction();
    
    /**
     * Substitutes variables in the predicate  with passed expressions
     * For special predicates, a check on the expression type is carried out,
     * and if expression is not in the right form, an exception is thrown.
     * @param varNames
     * @param expressions
     * @return 
     */
    public Predicate substitute(ArrayList<String> varNames, ArrayList<Expression> expressions);

    /**
     * converts the function to a MathML representation, using 
     * JSBML library
     * @return  an ASTNode 
     */
    public ASTNode convertToJSBML();
    
    
    
     /**
     * evaluates if the predicate can change value continuously,
     * i.e. by means of the evolution of continuous quantity.
     * The continuity status is in the store
     * @return 
     */
    public boolean changeContinuously();
    /**
     * evaluates if the predicate can change value continuously,
     * i.e. by means of the evolution of continuous quantity.
     * The continuity status is in variables passed as a parameter
     * @param vars the value of variables
     * @return the truth value of the predicate
     */
    public boolean changeContinuously(SymbolArray vars);


        /**
     * Clones the predicate with respect to a new Store, adding it to the new store,
     * @param newStore
     * @return 
     */
    public Predicate clone(Store newStore);
    
    /**
     * Converts the predicate to a matlab expression
     * @return 
     */
    public String toMatlabCode();

}


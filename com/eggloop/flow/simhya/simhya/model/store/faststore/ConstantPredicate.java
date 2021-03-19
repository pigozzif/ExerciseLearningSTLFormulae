/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore;

import org.sbml.jsbml.ASTNode;
import  com.eggloop.flow.simhya.simhya.matheval.Expression;
import  com.eggloop.flow.simhya.simhya.model.store.Function;
import  com.eggloop.flow.simhya.simhya.model.store.Predicate;
import java.util.ArrayList;
import  com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import  com.eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class ConstantPredicate implements Predicate {
    private boolean value;

    public ConstantPredicate(boolean value) {
        this.value = value;
    }


    public boolean evaluate() {
        return value;
    }

    public boolean evaluate(SymbolArray vars) {
        return value;
    }

    public boolean evaluateCache() {
        return value;
    }

    public boolean evaluateCache(SymbolArray vars) {
        return value;
    }

    public ArrayList<Integer> getVariableList() {
        return new ArrayList<Integer>();
    }

    public void initialize() {  }

    
    public boolean isContradiction() {
        return !value;
    }

    public boolean isTautology() {
        return value;
    }

    public String toModelLanguage() {
        if (value)
            return "";
        else
            return "1 != 0";

    }

    public boolean canBeConvertedToFunction() {
        return true;
    }

    public Function convertToFunction() {
        if (this.value) 
            return new ConstantFunction(1.0);
        else
            return new ConstantFunction(-1.0);
    }

    public Predicate substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        return new ConstantPredicate(this.value);
    }

    public ASTNode convertToJSBML() {
        ASTNode n,n1;
        //defining parameter nodes
        n1 = new ASTNode(ASTNode.Type.CONSTANT_TRUE);
        if (!this.value) {
            n = new ASTNode(ASTNode.Type.LOGICAL_NOT);
            n.addChild(n1);
            return n;
        } else return n1;
    }

    public boolean changeContinuously() {
        return false;
    }

    public boolean changeContinuously(SymbolArray vars) {
        return false;
    }

    public Predicate clone(Store newStore) {
        return new ConstantPredicate(this.value);
    }
    
    public String toMatlabCode() {
        return (this.value ? "1" : "0" );
    }

    
    

}

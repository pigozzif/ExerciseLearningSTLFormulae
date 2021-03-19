/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore;

import org.sbml.jsbml.ASTNode;
import  com.eggloop.flow.simhya.simhya.matheval.Expression;
import  com.eggloop.flow.simhya.simhya.model.store.Function;
import java.util.ArrayList;
import  com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import  com.eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class ConstantFunction implements Function {
    private double value;
    private boolean zero;

    public ConstantFunction(double value) {
        this.value = value;
        if (value == 0)
            zero = true;
        else zero = false;
    }

    public double compute() {
        return value;
    }

    public double compute(SymbolArray vars) {
        return value;
    }

     public double computeCache() {
        return value;
    }

    public double computeCache(SymbolArray vars) {
        return value;
    }
    
    public ArrayList<Integer> getVariableList() {
        return new ArrayList<Integer>();
    }

    public void initialize() {  }

    public String toModelLanguage() {
        return "#con(" + value + ")";
    }

    public double getConstantIncrement() {
        throw new UnsupportedOperationException("This function is not a constant increment function,"
                + "please call isConstantIncrementFunction() before this function");
    }

    public boolean isConstantIncrementFunction() {
        return false;
    }

    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        ConstantFunction f = new ConstantFunction(this.value);
        return f;
    }

    public ASTNode convertToJSBML() {
        ASTNode n;
        n = new ASTNode(ASTNode.Type.REAL);
        n.setValue(this.value);
        return n;
    }

    public Function clone(Store newStore) {
        return new ConstantFunction(this.value);
    }

    public Function differentiate(String x) {
        return new ConstantFunction(0);
    }

    public boolean isConstantZero() {
        return zero;
    }

    public String toMatlabCode() {
        return "" + value;
    }
    
    
    
    
    
    
  
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore;
import java.util.ArrayList;
import org.sbml.jsbml.ASTNode;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.model.store.Store;
import com.eggloop.flow.simhya.simhya.model.store.Function;
import com.eggloop.flow.simhya.simhya.model.store.StoreException;
import com.eggloop.flow.simhya.simhya.model.store.Predicate;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.matheval.CompiledExpression;
import com.eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public class GenericPredicate implements Predicate {
   private Expression exp;
   private boolean compiled;
   private CompiledExpression cexp;
   private Store store;

    public GenericPredicate(Store store,Expression exp) {
        if (!exp.isLogicalExpression())
            throw new StoreException("The expression defining the predicate is not a logical expression: " + exp.toString());
        this.exp = exp;
        this.store = store;
        store.addExternalPredicate(this);
        if (GlobalOptions.compileMath) {
            cexp = exp.compileExpression();
            compiled = true;
        } else {
            this.compiled = false;
        }
    }

    public boolean evaluate() {
        if (compiled)
            return cexp.evaluateBool();
        else
            if (exp.computeValue() == 1.0) return true;
            else return false;
    }

    public boolean evaluate(SymbolArray vars) {
        if (compiled)
            return cexp.evaluateBool(vars);
        else
            if (exp.computeValue(vars) == 1.0) return true;
            else return false;
    }

    public boolean evaluateCache() {
        if (compiled)
            return cexp.evaluateBool();
        else
            if (exp.computeValueCache() == 1.0) return true;
            else return false;
    }

    public boolean evaluateCache(SymbolArray vars) {
        if (compiled)
            return cexp.evaluateBool(vars);
        else
            if (exp.computeValueCache(vars) == 1.0) return true;
            else return false;
    }

    public ArrayList<Integer> getVariableList() {
        return exp.getListOfVariables();
    }

    public void initialize() {  
        if (compiled) {
            cexp.setVariableReference(store.getVariablesReference().getReferenceToValuesArray());
            cexp.setParameterReference(store.getParametersReference().getReferenceToValuesArray());
        } 
    }

    public boolean isContradiction() {
        return false;
    }

    public boolean isTautology() {
        return false;
    }

    public String toModelLanguage() {
        return this.exp.toString();
    }

    public boolean canBeConvertedToFunction() {
        return this.exp.canBeConvertedToNumericalExpression();
    }

    public Function convertToFunction() {
        Function f = new GenericFunction(store,this.exp.convertToNumericalExpression());
        return f;
    }

    public Predicate substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        Expression newExp = this.exp.substitute(varNames, expressions);
        return new GenericPredicate(store,newExp);
    }

    public ASTNode convertToJSBML() {
        return this.exp.convertToJSBML();
    }
    
    public boolean changeContinuously() {
        return exp.computeContinuousChangeStatus();
    }

    public boolean changeContinuously(SymbolArray vars) {
        //System.out.println("Predicate " + exp.toString() + " change continuously? " +  exp.computeContinuousChangeStatus(vars));
        return exp.computeContinuousChangeStatus(vars);
    }

    public Predicate clone(Store newStore) {
        if (! (newStore instanceof FastStore))
            throw new StoreException("I cannot clone a FastStore predicate w.r.t a non fast store");
        FastStore s = (FastStore)newStore;
        GenericPredicate p = new GenericPredicate(s,this.exp.clone(s.eval));
        s.addExternalPredicate(p);
        return p;
    }
    
    
    public String toMatlabCode() {
        return this.exp.toMatlabCode();
    }
    
}

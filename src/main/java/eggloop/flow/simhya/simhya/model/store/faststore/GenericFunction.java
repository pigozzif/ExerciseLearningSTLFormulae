/*

* To change this template, choose Tools | Templates* and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.store.faststore;

import java.util.ArrayList;
import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.CompiledExpression;
import eggloop.flow.simhya.simhya.GlobalOptions;
import eggloop.flow.simhya.simhya.model.store.StoreException;

/**
 *
 * @author Luca
 */
public class GenericFunction implements Function {
    private Expression exp;
    private boolean compiled;
    private CompiledExpression cexp;
    private Store store;

    public GenericFunction(Store store, Expression exp) {
        this.exp = exp;
        this.store = store;
        store.addExternalFunction(this);
        if (GlobalOptions.compileMath) {
            cexp = exp.compileExpression();
            compiled = true;
        } else {
            this.compiled = false;
        }
    }

    public double compute() {
        return (compiled ? cexp.evaluate() : exp.computeValue());
    }

    public double compute(SymbolArray vars) {
        return (compiled ? cexp.evaluate(vars) : exp.computeValue(vars));
    }

     public double computeCache() {
        return (compiled ? cexp.evaluate() : exp.computeValueCache());
    }

    public double computeCache(SymbolArray vars) {
        return (compiled ? cexp.evaluate(vars) : exp.computeValueCache(vars));
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


    public String toModelLanguage() {
        return this.exp.toString();
    }

    public double getConstantIncrement() {
        throw new UnsupportedOperationException("This function is not a constant increment function,"
                + "please call isConstantIncrementFunction() before this function");
    }

    public boolean isConstantIncrementFunction() {
        return false;
    }
    
    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        Expression newExp = this.exp.substitute(varNames, expressions);
        return new GenericFunction(store,newExp);
    }

    public ASTNode convertToJSBML() {
        return this.exp.convertToJSBML();
    }
    
    public Function clone(Store newStore) {
        if (! (newStore instanceof FastStore))
            throw new StoreException("I cannot clone a FastStore predicate w.r.t a non fast store");
        FastStore s = (FastStore)newStore;
        GenericFunction f = new GenericFunction(s,this.exp.clone(s.eval));
        s.addExternalFunction(f);
        return f;
    }

    public Function differentiate(String x) {
        Expression dx = this.exp.differentiate(x);
        if (dx.isConstantZero())
            return new ConstantFunction(0);
        else {
            Function f = new GenericFunction(store,dx);
            f.initialize();
            return f;
        }
    }

    public boolean isConstantZero() {
        return exp.isConstantZero();
    }
    
    public String toMatlabCode() {
        return this.exp.toMatlabCode();
    }
    
    
}

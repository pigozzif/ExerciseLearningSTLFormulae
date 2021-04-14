/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.transition;

import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.ModelException;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Store;
import org.sbml.jsbml.ASTNode;

import java.util.ArrayList;

/**
 * @author luca
 */
public class BasicAtomicReset implements AtomicReset {

    //this is needed to deal with template transitions!!!
    public String name;
    public int variable;
    public Function resetFunction;
    public double newValue;

    BasicAtomicReset(int var, String name, Function func) {
        this.variable = var;
        this.resetFunction = func;
        this.name = name;
    }

    public void computeNewValues() {
        newValue = resetFunction.compute();
    }

    public void computeNewValuesCache() {
        newValue = resetFunction.computeCache();
    }

    public void computeNewValues(SymbolArray variables) {
        newValue = resetFunction.compute(variables);
    }

    public void computeNewValuesCache(SymbolArray variables) {
        newValue = resetFunction.computeCache(variables);
    }

    public void updateStoreVariables(double[] variables) {
        variables[variable] = newValue;
    }

    public void updateStoreVariables(SymbolArray variables) {
        variables.setValue(variable, newValue);
    }

    public double getConstantIncrement() {
        return this.resetFunction.getConstantIncrement();
    }

    public boolean isConstantIncrement() {
        return this.resetFunction.isConstantIncrementFunction();
    }

    public ArrayList<Integer> getUpdatedVariables() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(variable);
        return list;
    }

    public boolean isSimpleResetForVariable(int variable) {
        return (this.variable == variable);
    }

    public String toModelLanguage(Store store) {
        String f = resetFunction.toModelLanguage();
        if (f.contains("+=") || f.contains("-="))
            return f;
        return store.getVariablesReference().getName(variable) + " = " + f;
    }


    public BasicAtomicReset substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        String newName;
        int newVar;
        Function newFunc;
        //identify the new variable code, if it has changed
        if (varNames.contains(this.name)) {
            int i = varNames.indexOf(name);
            Expression exp = expressions.get(i);
            if (!exp.isVariable())
                throw new ModelException("Trying to substitute an updated variable, " + name
                        + " in a template by a non-variable expression: " + exp.toString());
            newName = exp.toString();
            newVar = exp.getVariableCode();
        } else {
            newName = name;
            newVar = variable;
        }
        //substitute vars in the function!
        newFunc = this.resetFunction.substitute(varNames, expressions);
        return new BasicAtomicReset(newVar, newName, newFunc);
    }

    public ASTNode convertToJSBML() {
        return resetFunction.convertToJSBML();
    }

    public AtomicReset clone(Store newStore) {
        BasicAtomicReset r = new BasicAtomicReset(this.variable, this.name, this.resetFunction.clone(newStore));
        return r;
    }

    public String toMatlabCode() {
        return resetFunction.toMatlabCode();
    }


}

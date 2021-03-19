/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore;

import java.util.ArrayList;
import org.sbml.jsbml.ASTNode;
import  com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import  com.eggloop.flow.simhya.simhya.model.store.*;
import  com.eggloop.flow.simhya.simhya.matheval.Expression;

/**
 *
 * @author luca
 */
public class ConstantParametricIncrementFunction implements Function {
    private final int param;
    private final int var;
    private final SymbolArray variablesArray;
    private final SymbolArray parametersArray;
    private double[] variables;
    private double[] parameters;
    private final boolean increment;
    private Store store;
    private String varName,parName;

    public ConstantParametricIncrementFunction(Store store, int varID, int paramID, boolean increment) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.parametersArray = (SymbolArray)store.getParametersReference();
        this.var = varID;
        this.param = paramID;
        this.increment = increment;
        this.store = store;
        this.varName = variablesArray.getName(var);
        this.parName = this.parametersArray.getName(param);
        store.addExternalFunction(this);
    }
    
    public ConstantParametricIncrementFunction(Store store, String var, int paramID, boolean increment) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.parametersArray = (SymbolArray)store.getParametersReference();
        this.var = -1;
        this.param = paramID;
        this.increment = increment;
        this.store = store;
        this.varName = var;
        //store.addExternalFunction(this);
    }

     public void initialize() {
        this.variables = variablesArray.getReferenceToValuesArray();
        this.parameters = parametersArray.getReferenceToValuesArray();
    }

    public double compute() {
        return variables[var] + ( increment ?  parameters[param] : -parameters[param]);
//        return variablesArray.getValue(var) + increment;

    }

    public double compute(SymbolArray vars) {
        return vars.getValue(var) + ( increment ?  parameters[param] : -parameters[param]);
//        return vars.getValue(var) + increment;
    }

     public double computeCache() {
        return variables[var] + ( increment ?  parameters[param] : -parameters[param]);
//        return variablesArray.getValue(var) + increment;

    }

    public double computeCache(SymbolArray vars) {
        return vars.getValue(var) + ( increment ?  parameters[param] : -parameters[param]);
//        return vars.getValue(var) + increment;
    }

    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList();
        list.add(var);
        return list;
    }

    public String toModelLanguage() {
        if (increment)
            return this.variablesArray.getName(var) + " += " + this.parametersArray.getName(param);
        else
            return this.variablesArray.getName(var) + " -= "+ this.parametersArray.getName(param);
    }

    public double getConstantIncrement() {
        return ( increment ?  parameters[param] : -parameters[param]);
    }

    public boolean isConstantIncrementFunction() {
        return true;
    }
    
    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        if (var >= 0)
            return new ConstantParametricIncrementFunction(store,var,param,increment);
        else {
            String n;
            int v;
            if (varNames.contains(this.varName)) {
                int i = varNames.indexOf(varName);
                if (!expressions.get(i).isVariable())
                    throw new StoreException("Expression " + expressions.get(i).toString()  + " is not a variable node!");
                v = expressions.get(i).getVariableCode();
            } else 
                v = store.getVariableID(varName);
            return new ConstantParametricIncrementFunction(store,v,param,increment);
        }
    }

    public ASTNode convertToJSBML() {
        ASTNode n,np,nv;
        //defining parameter nodes
        np = new ASTNode(ASTNode.Type.NAME);
        np.setVariable(new org.sbml.jsbml.Parameter(this.parametersArray.getName(param)));
        nv = new ASTNode(ASTNode.Type.NAME);
        nv.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var)));
        n = ASTNode.sum(np,nv);
        return n;
    }
    
   public Function clone(Store newStore) {
        ConstantParametricIncrementFunction f = new ConstantParametricIncrementFunction(newStore,this.var,this.param,this.increment);
        newStore.addExternalFunction(f);
        return f;
    }

    public Function differentiate(String x) {
        if (x.equals(this.varName)) {
            return new ConstantFunction(1);
        } else if (x.equals(this.parName)) {
            if (this.increment)
                return new ConstantFunction(1);
                else return new ConstantFunction(-1);
        } else return new ConstantFunction(0);
    }

    public boolean isConstantZero() {
        return false;
    }

    public String toMatlabCode() {
        String s = "";
        s += (this.increment ? "" : "-");
        s += "par(" + (param+1) + ")";
        return s;
    }
   
   
}

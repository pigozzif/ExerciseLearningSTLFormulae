/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.store.faststore;

import java.util.ArrayList;
import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.*;
import eggloop.flow.simhya.simhya.matheval.Expression;

/**
 *
 * @author Luca
 */
public class ConstantIncrementFunction implements Function {
    private double increment;
    private int var;
    private SymbolArray variablesArray;
    private double[] variables;
    private Store store;
    private String varName;

    public ConstantIncrementFunction(Store store, int varID, double inc) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var = varID;
        this.increment = inc;
        this.store = store;
        this.varName = variablesArray.getName(var);
        store.addExternalFunction(this);
    }
    
    public ConstantIncrementFunction(Store store, String var, double inc) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var = -1;
        this.increment = inc;
        this.store = store;
        this.varName = var;
        //store.addExternalFunction(this);
    }

     public void initialize() {
        this.variables = variablesArray.getReferenceToValuesArray();
    }

    public double compute() {
        return variables[var] + increment;
//        return variablesArray.getValue(var) + increment;

    }

    public double compute(SymbolArray vars) {
        return vars.getValue(var) + increment;
//        return vars.getValue(var) + increment;
    }

     public double computeCache() {
        return variables[var] + increment;
//        return variablesArray.getValue(var) + increment;

    }

    public double computeCache(SymbolArray vars) {
        return vars.getValue(var) + increment;
//        return vars.getValue(var) + increment;
    }

    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList();
        list.add(var);
        return list;
    }

    public String toModelLanguage() {
        if (increment >= 0)
            return this.variablesArray.getName(var) + " += " + this.increment;
        else
            return this.variablesArray.getName(var) + " -= " + (-this.increment);
    }

    public double getConstantIncrement() {
        return increment;
    }

    public boolean isConstantIncrementFunction() {
        return true;
    }

    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        if (var >= 0)
            return new ConstantIncrementFunction(store,var,increment);
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
            return new ConstantIncrementFunction(store,v,increment);
        }
    }

    public ASTNode convertToJSBML() {
        ASTNode n,np,nv;
        //defining parameter nodes
        np = new ASTNode(ASTNode.Type.REAL);
        np.setValue(this.increment);
        nv = new ASTNode(ASTNode.Type.NAME);
        nv.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var)));
        n = ASTNode.sum(nv,np);
        return n;
    }

    public Function clone(Store newStore) {
        ConstantIncrementFunction f = new ConstantIncrementFunction(newStore,this.var,this.increment);
        newStore.addExternalFunction(f);
        return f;
    }
    
    public Function differentiate(String x) {
        if (x.equals(this.varName)) {
            return new ConstantFunction(1);
        } else return new ConstantFunction(0);
    }

    public boolean isConstantZero() {
        return false;
    }

    public String toMatlabCode() {
        String s = "";
        s += this.increment;
        return s;
    }
    
    

}

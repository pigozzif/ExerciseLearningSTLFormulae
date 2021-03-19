/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.model.store.faststore;
import java.util.ArrayList;
import java.util.Locale;
import org.sbml.jsbml.ASTNode;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.model.store.*;
import com.eggloop.flow.simhya.simhya.matheval.Expression;

/**
 *
 * @author Luca
 */
public class LinearConstantParameterFunction implements Function{
    private int var;
    private double par;
    private SymbolArray variablesArray;
    private double[] variables;
    private Store store;
    private String varName;

    public LinearConstantParameterFunction(Store store, double par, int var) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var = var;
        this.par = par;
        this.store = store;
        this.varName= variablesArray.getName(var);
        store.addExternalFunction(this);
    }
    
    public LinearConstantParameterFunction(Store store, double par, String var) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var = -1;
        this.par = par;
        this.store = store;
        this.varName= var;
        //store.addExternalFunction(this);
    }

     public void initialize() {
        this.variables = this.variablesArray.getReferenceToValuesArray();
    }

    public double compute() {
        return par * variables[var];
//        return parametersArray.getValue(par) * variablesArray.getValue(var);
    }

    public double compute(SymbolArray vars) {
        return par * vars.getValue(var);
//        return parametersArray.getValue(par)  * vars.getValue(var);
    }

     public double computeCache() {
        return par * variables[var];
//        return parametersArray.getValue(par) * variablesArray.getValue(var);
    }

    public double computeCache(SymbolArray vars) {
        return par * vars.getValue(var);
//        return parametersArray.getValue(par)  * vars.getValue(var);
    }

    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList();
        list.add(var);
        return list;
    }


    public String toModelLanguage() {
        String s =   String.format(Locale.UK, "%.6f", par) + "*" + varName;
//        String s =  "#lin(" + String.format(Locale.UK, "%.6f", par) + ",";
//        s += this.variablesArray.getName(var) +  ")";
        return s;
    }

    public double getConstantIncrement() {
        throw new UnsupportedOperationException("This function is not a constant increment function,"
                + "please call isConstantIncrementFunction() before this function");
    }

    public boolean isConstantIncrementFunction() {
        return false;
    }
    
    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
         if (var >= 0)
            return new LinearConstantParameterFunction(store,par,var);
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
            return new LinearConstantParameterFunction(store,par,v);
        }
    }

    public ASTNode convertToJSBML() {
        ASTNode n,np,nv;
        //defining parameter nodes
        np = new ASTNode(ASTNode.Type.REAL);
        np.setValue(par);
        nv = new ASTNode(ASTNode.Type.NAME);
        nv.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var)));
        n = ASTNode.times(np,nv);
        return n;
    }
    
    public Function clone(Store newStore) {
        Function f = new LinearConstantParameterFunction(newStore,this.par,this.var);
        newStore.addExternalFunction(f);
        return f;
    }

    public Function differentiate(String x) {
          if (x.equals(this.varName)) {
            Function f = new ConstantFunction(par);
            f.initialize();
            return f;
        } else  {
            return new ConstantFunction(0.0);
        } 
    }

    public boolean isConstantZero() {
        return par == 0;
    }
    
    
    public String toMatlabCode() {
        String s = "";
        s += "" + par + " * var(" + (var+1) + ")";
        return s;
    }
    
    
}

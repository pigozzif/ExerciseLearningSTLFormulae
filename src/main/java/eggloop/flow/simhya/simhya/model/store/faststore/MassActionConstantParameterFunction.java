/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.model.store.faststore;

import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.Expression;
import java.util.ArrayList;
import java.util.Locale;
import eggloop.flow.simhya. simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.*;

/**
 *
 * @author Luca
 */

/**
 *
 * @author Luca
 */
public class MassActionConstantParameterFunction implements Function {
    private int var1;
    private int var2;
    private double par;
    private SymbolArray variablesArray;
    private double[] variables;
    private Store store;
    private String varName1, varName2;

    public MassActionConstantParameterFunction(Store store, double par, int var1, int var2) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var1 = var1;
        this.var2 = var2;
        this.par = par;
        this.store = store;
        this.varName1 = variablesArray.getName(var1);
        this.varName2 = variablesArray.getName(var2);
        store.addExternalFunction(this);
    }

    public MassActionConstantParameterFunction(Store store, double par, String var1, String var2) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.var1 = -1;
        this.var2 = -1;
        this.par = par;
        this.store = store;
        this.varName1 = var1;
        this.varName2 = var2;
        //store.addExternalFunction(this);
    }
    
    
    public void initialize() {
        this.variables = this.variablesArray.getReferenceToValuesArray();
    }

    public double compute() {
       return par * variables[var1] * variables[var2];
//       return parametersArray.getValue(par) * variablesArray.getValue(var1) * variablesArray.getValue(var2);
    }

    public double compute(SymbolArray vars) {
        return par * vars.getValue(var1) * vars.getValue(var2);
//        return parametersArray.getValue(par) * vars.getValue(var1) * vars.getValue(var2);
    }

    public double computeCache() {
       return par * variables[var1] * variables[var2];
//       return parametersArray.getValue(par) * variablesArray.getValue(var1) * variablesArray.getValue(var2);
    }

    public double computeCache(SymbolArray vars) {
        return par * vars.getValue(var1) * vars.getValue(var2);
//        return parametersArray.getValue(par) * vars.getValue(var1) * vars.getValue(var2);
    }


    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList();
        list.add(var1);
        list.add(var2);
        return list;
    }


    public String toModelLanguage() {
        String s =   String.format(Locale.UK, "%.6f", par) + "*" + varName1 + "*" + varName2;
//        String s =  "#ma(" + String.format(Locale.UK, "%.6f", par) + ",";
//        s += this.variablesArray.getName(var1) + ",";
//        s += this.variablesArray.getName(var2) + ")";
        return s;
    }

    public double getConstantIncrement() {
        throw new UnsupportedOperationException("This function is not a constant increment function,"
                + "please call isConstantIncrementFunction() before this function");
    }

    public boolean isConstantIncrementFunction() {
        return false;
    }

    /**
     * Unsupported operation, a pointer to the same function will be returned
     * @param varNames
     * @param expressions
     * @return 
     */
    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        if (var1 >= 0 && var2 >= 0)
            return new MassActionConstantParameterFunction(store,par,var1,var2);
        else {
            String n1, n2;
            int v1,v2;
            if (varNames.contains(this.varName1)) {
                int i = varNames.indexOf(varName1);
                if (!expressions.get(i).isVariable())
                    throw new StoreException("Expression " + expressions.get(i).toString()  + " is not a variable node!");
                v1 = expressions.get(i).getVariableCode();
            } else 
                v1 = store.getVariableID(varName1);
            if (varNames.contains(this.varName2)) {
                int i = varNames.indexOf(varName2);
                if (!expressions.get(i).isVariable())
                    throw new StoreException("Expression " + expressions.get(i).toString()  + " is not a variable node!");
                v2 = expressions.get(i).getVariableCode();
            } else 
                v2 = store.getVariableID(varName2);
            return new MassActionConstantParameterFunction(store,par,v1,v2);
        }
                
    }

    public ASTNode convertToJSBML() {
        ASTNode n,np,nv1,nv2;
        //defining parameter nodes
        np = new ASTNode(ASTNode.Type.REAL);
        np.setValue(par);
        nv1 = new ASTNode(ASTNode.Type.NAME);
        nv1.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var1)));
        nv2 = new ASTNode(ASTNode.Type.NAME);
        nv2.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var2)));
        n = ASTNode.times(np,nv1,nv2);
        return n;
    }

    public Function clone(Store newStore) {
        Function f = new MassActionConstantParameterFunction(newStore,this.par,this.var1,this.var2);
        newStore.addExternalFunction(f);
        return f;
    }

    public Function differentiate(String x) {
        if (x.equals(this.varName1)) {
            Function f = new LinearConstantParameterFunction(store,par,var2);
            f.initialize();
            return f;
        } else if (x.equals(this.varName2)) {
            Function f = new LinearConstantParameterFunction(store,par,var1);
            f.initialize();
            return f; 
        } else 
            return new ConstantFunction(0.0);
    } 
        
    
    public boolean isConstantZero() {
        return (this.par == 0.0);
    }
    
    
    public String toMatlabCode() {
        String s = "";
        s += "" + par + " * var(" + (var1+1) + ") * var(" + (var2+1) + ")";
        return s;
    }
    

}


 
    

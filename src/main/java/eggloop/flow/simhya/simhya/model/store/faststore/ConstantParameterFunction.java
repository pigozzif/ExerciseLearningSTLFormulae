/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.model.store.faststore;


import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.model.store.Function;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.Store;


/**
 *
 * @author Luca
 */
public class ConstantParameterFunction implements Function {
    private int par;
    private SymbolArray parametersArray;
    private double[] parameters;
    private Store store;
    private String parName;
    

    public ConstantParameterFunction(Store store, int par) {
        this.parametersArray = (SymbolArray)store.getParametersReference();
        this.par = par;
        this.store = store;
        this.parName = this.parametersArray.getName(par);
        store.addExternalFunction(this);
    }

    public double compute() {
        return parameters[par];
    }

    public double compute(SymbolArray vars) {
        return parameters[par];
    }

     public double computeCache() {
        return parameters[par];
    }

    public double computeCache(SymbolArray vars) {
        return parameters[par];
    }
    
    public ArrayList<Integer> getVariableList() {
        return new ArrayList<Integer>();
    }

    public void initialize() {
        this.parameters = this.parametersArray.getReferenceToValuesArray();
    }

    public String toModelLanguage() {
        return  parName ;
        //return "#con(" + parName + ")";
    }

    public double getConstantIncrement() {
        throw new UnsupportedOperationException("This function is not a constant increment function,"
                + "please call isConstantIncrementFunction() before this function");
    }

    public boolean isConstantIncrementFunction() {
        return false;
    }

    public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
        ConstantParameterFunction f = new ConstantParameterFunction(store,par);
        return f;
    }

    public ASTNode convertToJSBML() {
        ASTNode n;
        n = new ASTNode(ASTNode.Type.NAME);
        n.setVariable(new org.sbml.jsbml.Parameter(parName));
        return n;
    }

    public Function clone(Store newStore) {
        return new ConstantParameterFunction(store,par);
    }

    public Function differentiate(String x) {
        if (x.equals(parName)) {
            return new ConstantFunction(1);
        } else return new ConstantFunction(0);
    }

    public boolean isConstantZero() {
        return parameters[par] == 0;
    }
    
    public String toMatlabCode() {
        return "par(" + (par+1) + ")";
    }
    
    
}

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
public class LinearFunction implements Function{
    private int var;
    private int par;
    private SymbolArray variablesArray;
    private SymbolArray parametersArray;
    private double[] variables;
    private double[] parameters;
    private Store store;
    private String varName,parName;

    public LinearFunction(Store store, int par, int var) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.parametersArray = (SymbolArray)store.getParametersReference();
        this.var = var;
        this.par = par;
        this.store = store;
        this.varName= variablesArray.getName(var);
        this.parName = this.parametersArray.getName(par);
        store.addExternalFunction(this);
    }
    
    public LinearFunction(Store store, int par, String var) {
        this.variablesArray = (SymbolArray)store.getVariablesReference();
        this.parametersArray = (SymbolArray)store.getParametersReference();
        this.var = -1;
        this.par = par;
        this.store = store;
        this.varName= var;
        //store.addExternalFunction(this);
    }

     public void initialize() {
        this.variables = this.variablesArray.getReferenceToValuesArray();
        this.parameters = this.parametersArray.getReferenceToValuesArray();
    }

    public double compute() {
        return parameters[par] * variables[var];
//        return parametersArray.getValue(par) * variablesArray.getValue(var);
    }

    public double compute(SymbolArray vars) {
        return parameters[par] * vars.getValue(var);
//        return parametersArray.getValue(par)  * vars.getValue(var);
    }

     public double computeCache() {
        return parameters[par] * variables[var];
//        return parametersArray.getValue(par) * variablesArray.getValue(var);
    }

    public double computeCache(SymbolArray vars) {
        return parameters[par] * vars.getValue(var);
//        return parametersArray.getValue(par)  * vars.getValue(var);
    }

    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList();
        list.add(var);
        return list;
    }


    public String toModelLanguage() {
        String s =  "#lin(" + this.parametersArray.getName(par) + ",";
        s += this.variablesArray.getName(var) +  ")";
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
            return new LinearFunction(store,par,var);
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
            return new LinearFunction(store,par,v);
        }
    }

    public ASTNode convertToJSBML() {
        ASTNode n,np,nv;
        //defining parameter nodes
        np = new ASTNode(ASTNode.Type.NAME);
        np.setVariable(new org.sbml.jsbml.Parameter(this.parametersArray.getName(par)));
        nv = new ASTNode(ASTNode.Type.NAME);
        nv.setVariable(new org.sbml.jsbml.Parameter(this.variablesArray.getName(this.var)));
        n = ASTNode.times(np,nv);
        return n;
    }
    
    public Function clone(Store newStore) {
        Function f = new LinearFunction(newStore,this.par,this.var);
        newStore.addExternalFunction(f);
        return f;
    }

    public Function differentiate(String x) {
        if (x.equals(this.varName)) {
            Function f = new ConstantParameterFunction(store,par);
            f.initialize();
            return f;
        } else if (x.equals(this.parName)) {
            Function f = new LinearConstantParameterFunction(store,1,var);
            f.initialize();
            return f;
        } else {
            return new ConstantFunction(0.0);
        } 
    }

    public boolean isConstantZero() {
        return parameters[par] == 0;
    }
    
    
    public String toMatlabCode() {
        String s = "";
        s += "par(" + (par+1) + ") * var(" + (var+1) + ")";
        return s;
    }
    
    
}

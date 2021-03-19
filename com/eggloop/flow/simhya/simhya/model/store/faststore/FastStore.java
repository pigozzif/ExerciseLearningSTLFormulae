/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.model.store.*;
import java.util.Locale;
import com.eggloop.flow.simhya.simhya.matheval.Evaluator;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.matheval.ExpressionSymbolArray;
import com.eggloop.flow.simhya.simhya.model.store.faststore.funclib.*;


/**
 *
 * @author Luca
 */
public class FastStore implements Store {
    Evaluator eval;
    private ArrayList<Predicate> externalPredicates;
    private ArrayList<Function> externalFunctions;
    private SymbolArray variables;
    private SymbolArray parameters;
    private ExpressionSymbolArray expressionVariables;
    private boolean initialized;
    private boolean variablesInitialized;
    private ArrayList<FunctionLibrary> functionLibraries;
    private ArrayList<String> functionDefinitions;

    public FastStore() {
        this.eval = new Evaluator();
        this.externalFunctions = new ArrayList<Function>();
        this.externalPredicates = new ArrayList<Predicate>();
        this.variables = eval.getVariableReference();
        this.parameters = eval.getConstantReference();
        this.expressionVariables = eval.getExpressionVariableReference();
        this.initialized = false;
        this.variablesInitialized = false;
        functionLibraries = new ArrayList<FunctionLibrary>();
        functionLibraries.add(new StandardFunctionLibrary());
        functionDefinitions = new ArrayList<String>();
    }

    public void addExternalFunction(Function f) {
        this.externalFunctions.add(f);
    }
    public void addExternalPredicate(Predicate p) {
        this.externalPredicates.add(p);
    }

    public int addParameter(String name, double value) {
        if (initialized)
            throw new StoreException("Store already initialized");
        if (variablesInitialized)
            throw new StoreException("Variables already initialized");
        if (variables.containsSymbol(name) || parameters.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new StoreException("Symbol " + name + " already defined");
        int id = eval.addConstant(name, value);
        return id;
    }

    public int addParameter(String name, double value, Expression expression) {
        if (initialized)
            throw new StoreException("Store already initialized");
        if (variablesInitialized)
            throw new StoreException("Variables already initialized");
        if (variables.containsSymbol(name) || parameters.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new StoreException("Symbol " + name + " already defined");
        if (expression  == null)
            throw new StoreException("Expression undefined");
        int id = eval.addConstant(name, value, expression);
        return id;
    }

    public int addVariable(String name, double value) {
        if (initialized)
            throw new StoreException("Store already initialized");
        if (variablesInitialized)
            throw new StoreException("Variables already initialized");
        if (variables.containsSymbol(name) || parameters.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new StoreException("Symbol " + name + " already defined");
        int id = eval.addVariable(name, value);
        return id;
    }
    public int addVariable(String name, double value, Expression expression) {
        if (initialized)
            throw new StoreException("Store already initialized");
        if (variablesInitialized)
            throw new StoreException("Variables already initialized");
        if (variables.containsSymbol(name) || parameters.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new StoreException("Symbol " + name + " already defined");
        if (expression  == null)
            throw new StoreException("Expression undefined");
        int id = eval.addVariable(name, value, expression);
        return id;
    }
    
    
    /**
     * Changes the expression for a variable, but does not recompute the value
     * of the variable
     * @param varId
     * @param expression 
     */
    public void changeExpressionForVariable(int varId, Expression expression) {
        this.eval.replaceInitialValueExpression(varId, expression);
    }

    public int addExpressionVariable(String name, Expression exp) {
        if (initialized)
            throw new StoreException("Store already initialized");
        if (variablesInitialized)
            throw new StoreException("Variables already initialized");
        if (variables.containsSymbol(name) || parameters.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new StoreException("Symbol " + name + " already defined");
        int id = eval.addExpressionVariable(name, exp);
        return id;
    }

    public void finalizeVariableInitialization() {
        variables.fixValueArray();
        parameters.fixValueArray();
        expressionVariables.fixValueArray();
        this.variablesInitialized = true;
    }
    public void finalizeInitialization() {
        for (Function f : this.externalFunctions)
            f.initialize();
        for (Predicate p : this.externalPredicates)
            p.initialize();
        this.initialized = true;
    }
    
    public ArrayList<String> getNameOfAllParameters() {
        return parameters.getNameOfAllSymbols();
    }
    public int getNumberOfParameters() {
        return parameters.getNumberOfSymbols();
    }
    public int getParameterID(String name) {
        return parameters.getSymbolId(name);
    }
    public double getParameterValue(int parameterID) {
        return parameters.getValue(parameterID);
    }
    
    public double[] getParametersValues() {
        return parameters.getReferenceToValuesArray();
    }
    public double[] getCopyOfParametersValues() {
        return parameters.getCopyOfValuesArray();
    }
    public void setAllParameterValues(ArrayList<Double> values) {
        int n = parameters.getNumberOfSymbols();
        if (values.size() != n)
            throw new StoreException("Incorrect number of parameters");
        for (int i=0;i<n;i++)
            parameters.setValue(i, values.get(i));
    }
    public void setAllParameterValues(double[] values) {
        int n = parameters.getNumberOfSymbols();
        if (values.length != n)
             throw new StoreException("Incorrect number of parameters");
        for (int i=0;i<n;i++)
            parameters.setValue(i, values[i]);
    }

  
     public void copyAllParameterValues(SymbolArray values) {
        this.parameters.copyValues(values);
    }

   
    public void setParameterValuesReference(SymbolArray values) {
        this.parameters.setValuesReference(values);
    }
    
    public void setParameterValue(int id, double value) {
        parameters.setValue(id, value);
    }

    public SymbolArray getParametersReference() {
        return this.parameters;
    }



    public ArrayList<String> getNameOfAllVariables() {
        return variables.getNameOfAllSymbols();
    }
    public int getNumberOfVariables() {
        return variables.getNumberOfSymbols();
    }
    public int getVariableID(String name) {
        return variables.getSymbolId(name);
    }
    public double getVariableValue(int id) {
        return variables.getValue(id);
    }
    
    public double[] getVariablesValues() {
        return variables.getReferenceToValuesArray();
    }
    public double[] getCopyOfVariablesValues() {
        return variables.getCopyOfValuesArray();
    }
    public void setAllVariableValues(ArrayList<Double> values) {
        int n =  variables.getNumberOfSymbols();
        if (values.size() < n)
            throw new StoreException("Too few variables!");
        for (int i=0;i<n;i++)
            variables.setValue(i, values.get(i));
    }
    public void setAllVariableValues(double[] values) {
        int n =  variables.getNumberOfSymbols();
        if (values.length < n)
            throw new StoreException("Too few variables!");
        for (int i=0;i<n;i++)
            variables.setValue(i, values[i]);
    }
    public void setVariableValue(int id, double value) {
        variables.setValue(id, value);
    }

    public String getVariablesRepresentation() {
        String s = "";
        ArrayList<String> names = variables.getNameOfAllSymbols();
        for (int i=0;i<this.variables.getNumberOfSymbols();i++)
            s += (i > 0 ? "; " : "") + String.format(Locale.US, "%s=%.5f", names.get(i), variables.getValue(i));
        return s;
    }

    public SymbolArray getVariablesReference() {
        return this.variables;
    }

   
    public void copyAllVariableValues(SymbolArray values) {
        variables.copyValues(values);
    }

    public void setVariableValuesReference(SymbolArray values) {
        variables.setValuesReference(values);
    }

    public Expression parseExpression(String expression) {
        int id = -1;
        try { id = eval.parse(expression); }
        catch(Exception e) { throw new StoreException("" + e); }
        if (id == -1)
            throw new StoreException("Failed to parse string " + expression);
        Expression exp = eval.getExpression(id);
        return exp;
    }

    public Expression parseExpression(String expression, SymbolArray localVariables) {
        int id = -1;
        try { id = eval.parse(expression,localVariables); }
        catch(Exception e) { throw new StoreException("" + e); }
        if (id == -1)
            throw new StoreException("Failed to parse string " + expression);
        Expression exp = eval.getExpression(id);
        return exp;
    }
    
    
    public Function addFunction(String function) {
        Expression exp = this.parseExpression(function);
        GenericFunction f = new GenericFunction(this,exp);
        return f;
    }
     public Function addFunction(String function, SymbolArray localVariable) {
        Expression exp = this.parseExpression(function,localVariable);
        GenericFunction f = new GenericFunction(this,exp);
        return f;
    }

    public void addNewFunctionDefinition(String functionDefinition) {
        try { eval.parse(functionDefinition);  this.functionDefinitions.add(functionDefinition); }
        catch(Exception e) { throw new StoreException(e.getMessage());}
    }
    public void addNewFunctionDefinition(String funcName, ArrayList<String> funcParams, String funcDef) {
        String f = "";
        f += funcName + "(";
        for (int i=0;i<funcParams.size();i++)
            f += (i>0?",":"") + funcParams.get(i);
        f+=") := " + funcDef;
        this.addNewFunctionDefinition(f);
    }
    public void addNewExpressionDefinition(String expressionDefinition) {
        try { eval.parse(expressionDefinition);  }
        catch(Exception e) { throw new StoreException(e.getMessage());}
    }
    public Predicate addPredicate(String predicate) {
        Expression exp = this.parseExpression(predicate);
        if (!exp.isLogicalExpression())
            throw new StoreException("Expression " + predicate + " is not a logical expression.");
        GenericPredicate p = new GenericPredicate(this,exp);
        return p;
    }

    public Predicate addPredicate(String predicate, SymbolArray localVariables) {
        Expression exp = this.parseExpression(predicate,localVariables);
        if (!exp.isLogicalExpression())
            throw new StoreException("Expression " + predicate + " is not a logical expression.");
        GenericPredicate p = new GenericPredicate(this,exp);
        return p;
    }

     public ArrayList<String> getFunctionDefinitions() {
         return this.functionDefinitions;
     }
    
    public void newEvaluationRound() {
        this.eval.newEvaluationCode();
    }

    public void addFunctionLibrary(String library) {
        System.err.println("There is only the standard library up to now...");
        return;
    }

    public Function getFunctionFromLibrary(String name, ArrayList<String> args) {
        FunctionLibrary library = null;
        for (FunctionLibrary lib : this.functionLibraries) {
            if (lib.isLibraryFunction(name, args.size())) {
                library = lib;
            }
        }
        if (library == null) throw new StoreException("There is no library function with name" + name + " and " + args.size() + " arguments");
        for (int i=0;i<args.size();i++) {
            if (!library.isArgumentCorrect(name, i, args.get(i), this))
                throw new StoreException("Argument " + i + " of " + name + " must be a " + library.getArgumentType(name, i));
        }
        return library.getLibraryFunction(name, args, this);
    }

    
    public Function getFunctionFromLibrary(String name, ArrayList<String> args, SymbolArray localVars) {
        FunctionLibrary library = null;
        for (FunctionLibrary lib : this.functionLibraries) {
            if (lib.isLibraryFunction(name, args.size())) {
                library = lib;
            }
        }
        if (library == null) throw new StoreException("There is no library function with name" + name + " and " + args.size() + " arguments");
        for (int i=0;i<args.size();i++) {
            if (!library.isArgumentCorrect(name, i, args.get(i), this, localVars))
                throw new StoreException("Argument " + i + " of " + name + " must be a " + library.getArgumentType(name, i));
        }
        return library.getLibraryFunction(name, args, this, localVars);
    }


    
    public int getNumberOfExpressionVariables() {
        return expressionVariables.getNumberOfSymbols();
    }
    public int getExpressionVariableID(String name) {
        return expressionVariables.getSymbolId(name);
    }
    public ArrayList<String> getNameOfAllExpressionVariables() {
        return expressionVariables.getNameOfAllSymbols();
    }
    public double getExpressionVariableValue(int variableID) {
        return expressionVariables.getValue(variableID);
    }
    public ExpressionSymbolArray getExpressionVariablesReference() {
        return expressionVariables;
    }
    public double[] getExpressionVariablesValues() {
        return expressionVariables.getAllValues();
    }

    public ArrayList<Expression> generateExpressionForSymbol(ArrayList<String> names) {
        return eval.generateExpressionForSymbol(names);
    }

    public Expression generateExpressionForSymbol(String name) {
        return eval.generateExpressionForSymbol(name);
    }

    public void replaceInitialValueExpression(String name, Expression newExpr) {
        eval.replaceInitialValueExpression(name, newExpr);
    }

    public void replaceInitialValueExpression(int id, Expression newExpr) {
        eval.replaceInitialValueExpression(id, newExpr);
    }

    @Override
    public Store clone() {
        FastStore newStore = new FastStore();
        //cloning the evaluator and replacing it
        Evaluator newEval = this.eval.clone();
        newStore.eval = newEval;
        //replacing info
        newStore.variables = newEval.getVariableReference();
        newStore.parameters = newEval.getConstantReference();
        newStore.expressionVariables = newEval.getExpressionVariableReference();
        newStore.functionDefinitions = (ArrayList<String>)this.functionDefinitions.clone(); 
        return newStore;
    }

    public Evaluator getEvaluator() {
        return this.eval;
    }

    public String functionDefinitionsToMatlabCode() {
        return this.eval.functionDefinitionsToMatlabCode();
    }

    public String expressionDefinitionsToMatlabCode() {
        return this.eval.expressionDefinitionsToMatlabCode();
    }

    
    
    
    
    
    
    
}

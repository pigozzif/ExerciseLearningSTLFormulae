/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.store;

import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.ExpressionSymbolArray;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;

import java.util.ArrayList;

/**
 * @author Luca
 */
public interface Store {

    public Predicate addPredicate(String predicate);

    public Function addFunction(String function);

    public void addExternalPredicate(Predicate p);

    public void addExternalFunction(Function f);

    public Predicate addPredicate(String predicate, SymbolArray localVariables);

    public Function addFunction(String function, SymbolArray localVariables);

    public Expression parseExpression(String expression);

    public Expression parseExpression(String expression, SymbolArray localVariables);


    public int addVariable(String name, double value);

    public int addVariable(String name, double value, Expression expression);

    public int getNumberOfVariables();

    public int getVariableID(String name);

    public ArrayList<String> getNameOfAllVariables();

    public double getVariableValue(int variableID);

    public void setVariableValue(int variableID, double value);

    public SymbolArray getVariablesReference();

    public double[] getVariablesValues();

    public double[] getCopyOfVariablesValues();

    public void setAllVariableValues(ArrayList<Double> values);

    public void setAllVariableValues(double[] values);

    public void copyAllVariableValues(SymbolArray values);

    public void setVariableValuesReference(SymbolArray values);


    public int addParameter(String name, double value);

    public int addParameter(String name, double value, Expression expression);

    public int getNumberOfParameters();

    public int getParameterID(String name);

    public ArrayList<String> getNameOfAllParameters();

    public double getParameterValue(int parameterID);

    public void setParameterValue(int parameterID, double value);

    public SymbolArray getParametersReference();

    public double[] getParametersValues();

    public double[] getCopyOfParametersValues();

    public void setAllParameterValues(ArrayList<Double> values);

    public void setAllParameterValues(double[] values);

    public void copyAllParameterValues(SymbolArray values);

    public void setParameterValuesReference(SymbolArray values);


    public int addExpressionVariable(String name, Expression exp);

    public int getNumberOfExpressionVariables();

    public int getExpressionVariableID(String name);

    public ArrayList<String> getNameOfAllExpressionVariables();

    public double getExpressionVariableValue(int variableID);

    public ExpressionSymbolArray getExpressionVariablesReference();

    public double[] getExpressionVariablesValues();

    public String expressionDefinitionsToMatlabCode();


    public void addNewFunctionDefinition(String funcName, ArrayList<String> funcParams, String funcDef);

    public void addNewFunctionDefinition(String funcDef);

    public void addNewExpressionDefinition(String expressionDefinition);

    public ArrayList<String> getFunctionDefinitions();

    public void addFunctionLibrary(String library);

    public Function getFunctionFromLibrary(String name, ArrayList<String> args);

    public Function getFunctionFromLibrary(String name, ArrayList<String> args, SymbolArray localVars);

    public ArrayList<Expression> generateExpressionForSymbol(ArrayList<String> names);

    public Expression generateExpressionForSymbol(String name);

    public void replaceInitialValueExpression(String name, Expression newExpr);

    public void replaceInitialValueExpression(int id, Expression newExpr);

    public String functionDefinitionsToMatlabCode();

    public void newEvaluationRound();

    public String getVariablesRepresentation();

    public void finalizeInitialization();

    public void finalizeVariableInitialization();


    public Store clone();


}

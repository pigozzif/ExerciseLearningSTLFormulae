/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Luca
 */
public class SymbolArray {
    double[] values;
    boolean[] evolvingContinuously;
    ArrayList<String> names;
    int numberOfSymbols;
    HashMap<String, Integer> symbolLookupTable;
    ArrayList<Expression> initialValueExpression;


    public SymbolArray() {
        values = new double[16];
        evolvingContinuously = new boolean[16];
        names = new ArrayList<String>(16);
        numberOfSymbols = 0;
        symbolLookupTable = new HashMap<String, Integer>();
        initialValueExpression = new ArrayList<Expression>(16);
    }

    private SymbolArray(int size) {
        values = new double[size];
        evolvingContinuously = new boolean[size];
        names = null;
        symbolLookupTable = null;
        numberOfSymbols = size;
        initialValueExpression = null;
    }

    /**
     * use to get a symbolarray for fast eval of dynamics functions.
     *
     * @param size
     */
    public static SymbolArray getNewFastEvalSymbolArray(int size) {
        return new SymbolArray(size);
    }

    private int add(String name, double value) {
        if (this.symbolLookupTable.containsKey(name))
            throw new EvalException("Symbol " + name + " already defined");
        int id = this.numberOfSymbols;
        if (values.length == this.numberOfSymbols)
            this.values = Arrays.copyOf(values, 2 * values.length);
        if (evolvingContinuously.length == this.numberOfSymbols)
            evolvingContinuously = Arrays.copyOf(evolvingContinuously, 2 * evolvingContinuously.length);
        this.names.add(name);
        this.values[id] = value;
        this.evolvingContinuously[id] = false;
        this.symbolLookupTable.put(name, id);
        this.numberOfSymbols++;
        return id;
    }

    public int addSymbol(String name, double value) {

        //System.out.println("Added symbol " + name + " with value " +value);

        int id = add(name, value);
        this.initialValueExpression.add(null);
        return id;
    }

    public int addSymbol(String name, double value, Expression expression) {
        int id = add(name, value);
        this.initialValueExpression.add(expression);
        return id;
    }

    public String getName(int id) {
        if (id < 0 || id >= this.numberOfSymbols)
            throw new EvalException("There is no symbol with id " + id);
        return names.get(id);
    }

    public ArrayList<String> getNameOfAllSymbols() {
        return (ArrayList<String>) names.clone();
    }

    public int getSymbolId(String name) {
        Integer id = this.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Variable " + name + " undefined.");
        return id;
    }

    /**
     * returns true if there is a symbol defined with a given name;
     *
     * @param name
     * @return
     */
    public boolean containsSymbol(String name) {
        return this.symbolLookupTable.containsKey(name);
    }


    public boolean isDefined(String name) {
        return (this.symbolLookupTable.containsKey(name));
    }

    public double getValue(int id) {
        return this.values[id];
    }

    public boolean getContinuousEvolutionStatus(int id) {
        return this.evolvingContinuously[id];
    }

    public void setValue(int id, double x) {
        this.values[id] = x;
    }


    public void setContinuousEvolutionStatus(int id, boolean b) {
        this.evolvingContinuously[id] = b;
    }

    public int getNumberOfSymbols() {
        return this.numberOfSymbols;
    }

    public boolean checkSymbolIndex(int id) {
        if (id < 0 || id >= this.numberOfSymbols)
            return false;
        return true;
    }


    public void fixValueArray() {
        if (values == null)
            values = new double[this.numberOfSymbols];
        if (values.length != this.numberOfSymbols)
            values = Arrays.copyOf(values, numberOfSymbols);
        if (evolvingContinuously == null)
            evolvingContinuously = new boolean[this.numberOfSymbols];
        if (evolvingContinuously.length != this.numberOfSymbols)
            evolvingContinuously = Arrays.copyOf(evolvingContinuously, numberOfSymbols);
    }

    public boolean[] getReferenceToContinuousEvolutionArray() {
        return this.evolvingContinuously;
    }

    public boolean[] getCopyOfContinuousEvolutionArray() {
        return Arrays.copyOf(evolvingContinuously, numberOfSymbols);
    }

    public double[] getReferenceToValuesArray() {
        return this.values;
    }

    public double[] getCopyOfValuesArray() {
        return Arrays.copyOf(values, numberOfSymbols);
    }


    public void copyValues(SymbolArray array) {
        if (array.numberOfSymbols != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.values = Arrays.copyOf(array.values, numberOfSymbols);
    }

    public void setValuesReference(SymbolArray array) {
        if (array.numberOfSymbols != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.values = array.values;
    }

    public void setValuesReference(double[] array) {
        if (array.length != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.values = array;
    }

    public void setContinuousEvolutionReference(SymbolArray array) {
        if (array.numberOfSymbols != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.evolvingContinuously = array.evolvingContinuously;
    }

    public void setContinuousEvolutionReference(boolean[] array) {
//removing this control, because it gets violated in the hybrid simulator, by adding new variables.
//
//        if (array.length  != this.numberOfSymbols)
//            throw new EvalException("Number of symbols mismatch");
        this.evolvingContinuously = array;
    }


    public void setAllValues(double[] array) {
        if (array.length != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.values = Arrays.copyOf(array, numberOfSymbols);
    }

    public void setAllContinuousEvolution(boolean[] array) {
        if (array.length != this.numberOfSymbols)
            throw new EvalException("Number of symbols mismatch");
        this.evolvingContinuously = Arrays.copyOf(array, numberOfSymbols);
    }

    public boolean hasExpressionForInitialValue(String name) {
        int id = this.getSymbolId(name);
        return (this.initialValueExpression.get(id) != null);

    }

    public boolean hasExpressionForInitialValue(int id) {
        return (this.initialValueExpression.get(id) != null);
    }

    public void evaluateAndStoreInitialValueExpressions() {
        for (int i = 0; i < this.numberOfSymbols; i++) {
            Expression exp = initialValueExpression.get(i);
            if (exp != null)
                this.values[i] = exp.computeValue();
        }
    }

    public double evaluateInitialValueExpression(int id) {
        Expression exp = initialValueExpression.get(id);
        if (exp != null)
            return exp.computeValue();
        else
            throw new EvalException("Symbol " + id + " has no initial value expression attached to it");
    }

    public double evaluateInitialValueExpression(String name) {
        int id = this.getSymbolId(name);
        Expression exp = initialValueExpression.get(id);
        if (exp != null)
            return exp.computeValue();
        else
            throw new EvalException("Symbol " + name + " has no initial value expression attached to it");
    }


    public Expression getInitialValueExpression(String name) {
        return this.getInitialExpression(this.getSymbolId(name));
    }

    public Expression getInitialValueExpression(int id) {
        return this.initialValueExpression.get(id);
    }


    /**
     * Replaces an initial value expression for the given variable.
     *
     * @param name
     * @param newExpr
     */
    void replaceInitialValueExpression(String name, Expression newExpr) {
        if (!this.names.contains(name))
            throw new EvalException("Variable " + name + " does not exist");
        int id = this.symbolLookupTable.get(name);
        this.replaceInitialValueExpression(id, newExpr);
    }

    /**
     * Replaces the initial value expression for the variable with given id.s
     *
     * @param id
     * @param newExpr
     */
    void replaceInitialValueExpression(int id, Expression newExpr) {
        if (id < this.numberOfSymbols)
            this.initialValueExpression.set(id, newExpr);
        else
            throw new EvalException("Id " + id + " out of bounds");
    }

    /**
     * removes a symbol from the array. Use with care,
     * as it changes symbol codes
     *
     * @param symbol the symbol to remove
     */
    void removeSymbol(String symbol) {
        if (!this.names.contains(symbol))
            throw new EvalException("Symbol array does not contain " + symbol);
        int i = this.symbolLookupTable.get(symbol);
        this.names.remove(i);
        this.initialValueExpression.remove(i);
        this.symbolLookupTable.remove(symbol);
        for (int j = i + 1; j < this.numberOfSymbols; j++)
            values[j - 1] = values[j];
        this.numberOfSymbols--;
        values = Arrays.copyOf(values, numberOfSymbols);
        for (int j = 0; j < this.numberOfSymbols; j++)
            this.symbolLookupTable.put(names.get(j), j);
    }

    Expression getInitialExpression(int id) {
        if (id < this.numberOfSymbols)
            return this.initialValueExpression.get(id);
        else
            throw new EvalException("Id " + id + " out of bounds");
    }


    /**
     * Clones the symbol array
     *
     * @param ignoreInitialExpression if true, all initial expressions will be ignored
     * @return a shallow copy of the array of symbols.
     */
    public SymbolArray clone(boolean ignoreInitialExpression) {
        SymbolArray newArray = new SymbolArray();
        if (this.names == null)
            return SymbolArray.getNewFastEvalSymbolArray(numberOfSymbols);
        for (int i = 0; i < this.numberOfSymbols; i++)
            if (!ignoreInitialExpression && this.initialValueExpression.get(i) != null)
                newArray.addSymbol(names.get(i), values[i], initialValueExpression.get(i));
            else
                newArray.addSymbol(names.get(i), values[i]);
        newArray.fixValueArray();
        return newArray;
    }


}

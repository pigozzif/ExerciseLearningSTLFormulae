/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;
import java.util.Arrays;

/**
 *
 * @author Luca
 */
public class ExpressionSymbolArray extends SymbolArray {
    Expression[] values;

    public ExpressionSymbolArray() {
        super();
        values = new Expression[16];
    }

    @Override
    public int addSymbol(String name, double value) {
        throw new EvalException("SymbolExpressionArray does not support the addition of symbols taking double values");
    }

    
    public int addSymbol(String name, Expression exp) {
        if (this.symbolLookupTable.containsKey(name))
            throw new EvalException("Symbol " + name + " already defined");
        int id = this.numberOfSymbols;
        if (values.length == this.numberOfSymbols)
            this.values = Arrays.copyOf(values, 2*values.length);
        if (evolvingContinuously.length == this.numberOfSymbols)
            evolvingContinuously = Arrays.copyOf(evolvingContinuously, 2*evolvingContinuously.length);
        this.names.add(name);
        this.values[id] = exp;
        this.evolvingContinuously[id] = false;
        this.symbolLookupTable.put(name, id);
        this.numberOfSymbols++;
        return id;
    }

    @Override
    public void copyValues(SymbolArray array) {
        throw new EvalException("SymbolExpressionArray does not support this operation");
    }

    @Override
    public double[] getCopyOfValuesArray() {
        throw new EvalException("SymbolExpressionArray does not support this operation");
    }

    public Expression[] getCopyOfExpressionArray() {
        return Arrays.copyOf(values, numberOfSymbols);
    }

    @Override
    public double[] getReferenceToValuesArray() {
        throw new EvalException("SymbolExpressionArray does not support this operation");
    }

    public Expression[] getReferenceToExpressionArray() {
        return values;
    }

    public Expression getExpression(int id) {
        return values[id];
    }


    @Override
    public double getValue(int id) {
        return values[id].computeValue();
    }

    @Override
    public void setValue(int id, double x) {
        throw new EvalException("SymbolExpressionArray does not support this operation");
    }

    @Override
    public void setValuesReference(SymbolArray array) {
        throw new EvalException("SymbolExpressionArray does not support this operation");
    }


    /**
     * computes all expressions and returns a double array containing all their values.
     * @return
     */
    public double[] getAllValues() {
        double[] v = new double[this.numberOfSymbols];
        for (int i=0;i<this.numberOfSymbols;i++)
            v[i] = this.values[i].computeValue();
        return v;
    }

    
    



}

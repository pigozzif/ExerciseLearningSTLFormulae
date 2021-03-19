/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.model.store.Store;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Luca
 */
public class MTLatom  extends MTLnode {
    String predicateString;
    Expression predicate;
    Expression numericalPredicate;
    boolean constraint;

    public MTLatom(String predicateString, boolean constraint) {
        super();
        this.predicateString = predicateString;
        this.predicate = null;
        numericalPredicate = null;
        this.constraint = constraint;
    }
    
    public MTLatom(long id, String predicateString, boolean constraint) {
        super(id);
        this.predicateString = predicateString;
        this.predicate = null;
        numericalPredicate = null;
        this.constraint = constraint;
    }
    
    
    @Override
    public MTLnode duplicate() {
        MTLnode n = new MTLatom(this.predicateString,true);
        return n;
    }

    @Override
    public NodeType getType() {
        return NodeType.ATOM;
    }
    
    
    public String getPredicateString() {
        return this.predicateString;
    }
    
    public void setPredicateString(String s) {
        this.predicateString = s;
    }
    
    
    @Override
    public void initalize(Store store, SymbolArray localSymbols, HashMap predicatesToExpression) {
        if (this.constraint) {
            predicate = store.parseExpression(predicateString, localSymbols);
            predicate.setLocalSymbols(localSymbols);
        } else {
            predicate = (Expression)predicatesToExpression.get(predicateString);
            if (predicate == null)
                throw new RuntimeException("Atomic predicate " + predicateString + " undefined");
        }  
        if (predicate.canBeConvertedToNumericalExpression())
                this.numericalPredicate = predicate.convertToNumericalExpression();
    }
    
    @Override
    boolean containsParameter(String param) {
        boolean a =  super.containsParameter(param);
        a = a || this.predicate.containsConstant(param);
        return a;
    }
    
    
    @Override
    public String toString() {
        return this.predicateString;
    }

    @Override
    public String toSign(){
        if (predicateString.contains(">")){
            return "1";
        }else if (predicateString.contains("<")){
            return "0";
        }
        return "";
    }

    @Override
     public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += this.predicateString + "\n";
        return s;
    }

    @Override
    ParametricInterval setExplorableTopNodes(String parameter) {
        if (this.isTopNode) {
            ParametricInterval I = new ParametricInterval();
            I.setUndefined();
            return I;
        } else return null;
    }

    @Override
    void labelingFormulaPointwiseSemantics(double[] deltaTimes) {
        //labelling has already been done for atoms
        return;
    }

    @Override
    void labelingTopFormulaPointwiseSemantics(double[] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {
        //labelling has already been done for atoms
        return;
    }

    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        return;
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        this.truthValue[0] = (signal.getValueAt(0) ? Truth.TRUE : Truth.FALSE);
    }

    
    
    

    @Override
    void collectAtomicNodes(ArrayList<MTLatom> list) {
        list.add(this);
    }
    
    void computeTruthValue(int i) {
        //we avoid computing the truth value when not needed!
        if (i < truthValue.length) {
            double x = this.predicate.computeValue();
            if (x == 1.0)
                this.truthValue[i] = Truth.TRUE;
            else this.truthValue[i] = Truth.FALSE;
        }
    }
    
    
    double computePredicateValue() {
        if (this.numericalPredicate == null) 
            throw new RuntimeException("predicate not converted to numerical formula");
        return numericalPredicate.computeValue();
    }
    
    
    boolean isStrictInequality() {
        return this.predicate.isStrictInequality();
    }
    
    

    @Override
    public boolean isAtom() {
        return true;
    }
    
    
    
    
    
}

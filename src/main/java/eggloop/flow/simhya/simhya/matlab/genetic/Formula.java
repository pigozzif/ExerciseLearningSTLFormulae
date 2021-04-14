/*
 * Wraps a MiTL formula to use in the genetic optimisation via matlab
 */
package eggloop.flow.simhya.simhya.matlab.genetic;

import java.util.ArrayList;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.modelchecking.mtl.MTLformula;
import eggloop.flow.simhya.simhya.modelchecking.mtl.MTLnode;


/**
 *
 * @author lucae
 */
public class Formula {
    MTLformula formula;

    public Formula(MTLformula formula) {
        this.formula = formula;
    }

    @Override
    public String toString() {
        return formula.toString();
    }

    public String toSign() {
        return formula.toSign();
    }

    
    
    public String toFormulaTree() {
        return formula.toFormulaTree();
    }
    
    
    void setNewRoot(MTLnode n) {
        this.formula.setNewRoot(n);
    }
    
    void refresh() {
        this.formula.refresh();
    }
    
    
    void recomputeBounds(SymbolArray localSymbols) {
        this.formula.recomputeBounds(localSymbols);
    }
    
    
   public int getFormulaSize() {
       return this.formula.countNodes();
   }
   
   public int getNumberOfBooleanNodes() {
       return this.formula.countBooleanNodes();
   }
   
   public int getNumberOfTemporalNodes() {
       return this.formula.countTemporalNodes();
   }
   
   public int getNumberOfAtomicNodes() {
       return this.formula.countLeaves();
   }
   
    MTLnode chooseInternalNode() {
        return this.formula.randomInternalNode();
    }

    MTLnode chooseRootNode() {
        return this.formula.getRoot();
    }
    
    MTLnode chooseOneNode() {
        return this.formula.randomNode();
    }
    
    ArrayList<MTLnode> getAllNodes() {
        return this.formula.getAllNodes();
    }
    
    
    public String[] getParameters() {
        ArrayList<String> list = this.formula.getParameters();
        for (String s : formula.getAtomicExpressions()) {
              list.add(this.getThreshold(s));
        }
        return list.toArray(new String[0]);
    } 
    
    public String[] getTimeBounds() {
        ArrayList<String> list = this.formula.getParameters();
        return list.toArray(new String[0]);
    } 
    
    public String[] getThresholds() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : formula.getAtomicExpressions()) {
              list.add(this.getThreshold(s));
        }
        return list.toArray(new String[0]);
    } 
    
    public String[] getVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : formula.getAtomicExpressions()) {
              list.add(this.getVariable(s));
        }
        return list.toArray(new String[0]);
    }
    
    private String getVariable(String atomicExpression) {
        int i = atomicExpression.indexOf("<");
        if (i<0) i = atomicExpression.indexOf(">");
        //there is one space between the variable and the comparison symbol
        return atomicExpression.substring(0, i-1);
    }
    
    private String getThreshold(String atomicExpression) {
        int i = atomicExpression.lastIndexOf("_");
        String p = GeneticOptions.threshold_name + atomicExpression.substring(i);
        return p;
    }
    
}

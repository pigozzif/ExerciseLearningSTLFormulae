/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignalTransducer;

/**
 *
 * @author Luca
 */
public class MTLnot  extends MTLnode {
    
     public MTLnot(MTLnode child) {
        super();
        super.setChild(child);
        child.setParent(this);
    }
    
     @Override
    public NodeType getType() {
        return NodeType.NOT;
    }
     
    @Override
    int countBooleanNodes() {
        return super.countBooleanNodes() + 1;
    }
     
     
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = child.duplicate();
        MTLnode n = new MTLnot(n1);
        return n;
    } 
     
     @Override
      public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "NOT\n";
        return s + super.toFormulaTree(depth);
    }

//    @Override
//    public String toString() {
//        String s = " NOT " + child.toString() + "";
//        return s;
//    }

    @Override
    public String toString() {
        String s = "!(" + child.toString() + ")";
        return s;
    }

    @Override
    public String toSign() {
        return child.toSign();
    }

    @Override
    ParametricInterval setExplorableTopNodes(String parameter) {
        if (this.isTopNode) {
            return child.setExplorableTopNodes(parameter);
        } else return null;
    }
     
     @Override
    void labelingFormulaPointwiseSemantics(double[] deltaTimes) {
        for (int i=0;i<this.traceLength;i++)
            this.truthValue[i] = Truth.not(child.truthValue[i]);
    }

    @Override
    void labelingTopFormulaPointwiseSemantics(double[] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {   
        this.labelingTop();
    }

    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = BooleanSignalTransducer.not(child.signal); 
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        this.labelingFormulaSignalSemantics(t0, tf);
        this.labelingTop();
    }
    
    void labelingTop() {
        for (int i=0;i<child.truthValue.length;i++)
            this.truthValue[i] = Truth.not(child.truthValue[i]);
    }
    

    
}

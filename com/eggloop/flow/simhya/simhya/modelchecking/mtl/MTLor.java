/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl;

import com.eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignalTransducer;

/**
 *
 * @author Luca
 */
public class MTLor  extends MTLnode {
    
    public MTLor(MTLnode left, MTLnode right) {
        super();
        super.setChildren(left,right);
        left.setParent(this);
        right.setParent(this);
    }
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = left.duplicate();
        MTLnode n2 = right.duplicate();
        MTLnode n = new MTLor(n1,n2);
        return n;
    }
    
    @Override
    public NodeType getType() {
        return NodeType.OR;
    }
    
    @Override
    int countBooleanNodes() {
        return super.countBooleanNodes() + 1;
    }
    
    @Override
     public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "OR\n";
        return s + super.toFormulaTree(depth);
    }
    
//    @Override
//    public String toString() {
//        String s = "(" + left.toString() + " OR " + right.toString() + ")";
//        return s;
//    }

    @Override
    public String toString() {
        String s = "(" + left.toString() + " | " + right.toString() + ")";
        return s;
    }

    @Override
    public String toSign() {
        return left.toSign()+right.toSign();
    }
    

    @Override
    ParametricInterval setExplorableTopNodes(String parameter) {
        if (this.isTopNode) {
           ParametricInterval l = left.setExplorableTopNodes(parameter);
           ParametricInterval r = right.setExplorableTopNodes(parameter);
           if (!l.equals(r))
               return null;
           else return l;
        } else return null;
    }

    @Override
    void labelingFormulaPointwiseSemantics(double[] deltaTimes) {
        for (int i=0;i<this.traceLength;i++)
            this.truthValue[i] = Truth.or(left.truthValue[i],right.truthValue[i]);
    }

    @Override
    void labelingTopFormulaPointwiseSemantics(double[] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {
        this.labelingTop(points);
    }

    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = BooleanSignalTransducer.or(left.signal, right.signal);
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        this.labelingFormulaSignalSemantics(t0, tf);
        this.labelingTop(points);
    }
    
    
    void labelingTop(int points) {
        if (left.truthValue.length == 1) {
            if (right.truthValue.length == 1)
                //case 1 - 1
                this.truthValue[0] = Truth.or(left.truthValue[0],right.truthValue[0]);
            else
                //case 1 - many
                for (int i=0;i<points;i++)
                    this.truthValue[i] = Truth.or(left.truthValue[0],right.truthValue[i]);
        } else {
            if (right.truthValue.length == 1)
                //case  many - 1
                for (int i=0;i<points;i++)
                    this.truthValue[i] = Truth.or(left.truthValue[i],right.truthValue[0]);
            else
                //case  many - many
                for (int i=0;i<points;i++)
                    this.truthValue[i] = Truth.or(left.truthValue[i],right.truthValue[i]);
        }    
    }
}

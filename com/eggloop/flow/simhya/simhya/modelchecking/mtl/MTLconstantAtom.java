/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl;

import com.eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignal;

/**
 *
 * @author Luca
 */
public class MTLconstantAtom extends MTLnode {
    boolean value;
    
    public MTLconstantAtom(boolean value) {
        this.value = value;
    }
    
    @Override
    public MTLnode duplicate() {
        MTLnode n = new MTLconstantAtom(true);
        return n;
    }
    
    @Override
    public NodeType getType() {
        return NodeType.CONSTANT_ATOM;
    }
    
     @Override
     public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += value + "\n";
        return s;
    }
     
     
     @Override
    public String toString() {
        String s = "" + this.value; 
        return s;
    }

    @Override
    public String toSign() {
       return "";
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
        for (int i=0;i<this.traceLength;i++)
            this.truthValue[i] = (value?Truth.TRUE:Truth.FALSE);
    }

    @Override
    void labelingTopFormulaPointwiseSemantics(double[] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {
        this.truthValue[0] = (value?Truth.TRUE:Truth.FALSE);
    }
    
    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = new BooleanSignal(t0,tf,value);
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        this.labelingFormulaSignalSemantics(t0, tf);
        this.truthValue[0] = (value ? Truth.TRUE : Truth.FALSE);
    } 
     
     
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignalTransducer;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.TimeInterval;



/**
 *
 * @author Luca
 */
public class MTLeventually extends MTLmodalNode {

    public MTLeventually(MTLnode child) {
        super(new ParametricInterval());
        getParametricInterval().setUpperValue(100);
        super.setChild(child);
        child.setParent(this);
    }
    
    public MTLeventually(ParametricInterval interval, MTLnode child) {
        super(interval);
        super.setChild(child);
        child.setParent(this);
    }
    
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = child.duplicate();
        MTLnode n = new MTLeventually(n1);
        return n;
    }
    
    
    @Override
    public NodeType getType() {
        return NodeType.EVENTUALLY;
    }

    @Override
    public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "Eventually " + parametricInterval.toString() + "\n";
        return s + super.toFormulaTree(depth);
    }

//    @Override
//    public String toString() {
//        String s = "F" + parametricInterval.toString() + " (" + child.toString() + ")";
//        return s;
//    }

    @Override
    public String toString() {
        String s = "F" + parametricInterval.toString() + " (" + child.toString() + ")";
        return s;
    }

    @Override
    public String toSign() {
        return child.toSign();
    }
    
    @Override
    boolean checkGoal(int j) {
        if (j < this.traceLength) 
            return child.truthValue[j] == Truth.TRUE;
        else return false;
    }

    @Override
    boolean checkSafe(int i) {
        return true;
    }

    @Override
    void setBad(int i) {
        this.truthValue[i] = Truth.FALSE;
    }

    @Override
    void setOk(int i) {
        this.truthValue[i] = Truth.TRUE;
    }

    
    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = BooleanSignalTransducer.eventually(child.signal, metricInterval.lower, metricInterval.upper);
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        this.labelingFormulaSignalSemantics(t0, tf);
        if (varyInterval && !metricInterval.upperIsInfinity &&
                metricInterval.lower < metricInterval.upper && points > 1) {
            //compute the span points
            double delta = (metricInterval.upper - metricInterval.lower)/(points-1);
            double [] span = new double[points];
            for (int i=0;i<points;i++) 
                span[i] = metricInterval.lower + i*delta;
            if (varyRightBound) {
                for (int i=0;i<points;i++)
                     this.truthValue[i] = BooleanSignalTransducer.eventuallyZero(child.signal, metricInterval.lower, span[i]);                
            } else {
               for (int i=0;i<points;i++)
                     this.truthValue[i] = BooleanSignalTransducer.eventuallyZero(child.signal, span[i], metricInterval.upper);                   
            }
        } else {
            if (signal.isUndefined())
                this.truthValue[0] = Truth.UNDEFINED;
            else
                this.truthValue[0] = (signal.getValueAt(0) ? Truth.TRUE : Truth.FALSE);
        }
        
        /*
        if (varyInterval && !metricInterval.upperIsInfinity &&
                metricInterval.lower < metricInterval.upper && points > 1) {
            //compute the span points
            double delta = (metricInterval.upper - metricInterval.lower)/(points-1);
            double [] span = new double[points];
            for (int i=0;i<points;i++) 
                span[i] = metricInterval.lower + i*delta;

            if (child.signal.isUndefined()) {
                for (int i=0;i<points;i++) 
                    this.truthValue[i] = Truth.UNDEFINED;
                return;
             }
            
            if (varyRightBound) {
                //finds the first time instant T after T1 in F[T1,T2] in which the 
                //child formula is true. for any [T1,t] containing T the formula is true
                TimeInterval I = child.signal.getFirstPositiveIntervalAfterT(metricInterval.lower);
                double T;
                if (I==null) {
                    if (child.signal.getFinalTime() < metricInterval.upper) {
                        for (int i=0;i<points;i++) 
                            this.truthValue[i] = Truth.UNDEFINED;
                        return;
                    } else 
                        T = metricInterval.upper+1;
                } else
                    T = I.getLower();
                
                for (int i=0;i<points;i++) 
                    if (span[i] > T)
                        this.truthValue[i] = Truth.TRUE;
                    else if (span[i] == T) 
                        this.truthValue[i] = ( I.isLowerClosed() ? Truth.TRUE : Truth.FALSE);
                    else    
                        this.truthValue[i] = Truth.FALSE;
            } else {
                //finds the largest T<=T2 such that the child signal is true in T.
                //the formula is true for each interval [t,T2] containing T.
                TimeInterval I = child.signal.getLastPositiveIntervalBeforeT(metricInterval.upper);
                double T;
                if (I==null) {
                    if (child.signal.getFinalTime() < metricInterval.upper) {
                        for (int i=0;i<points;i++) 
                            this.truthValue[i] = Truth.UNDEFINED;
                        return;
                    } else 
                        T = -1;
                } else
                    T = I.getUpper();
                for (int i=0;i<points;i++) 
                    if (span[i] < T)
                        this.truthValue[i] = Truth.TRUE;
                    else if (span[i] == T) 
                        this.truthValue[i] = (I.isUpperClosed() ? Truth.TRUE : Truth.FALSE);
                    else    
                        this.truthValue[i] = (child.signal.getFinalTime() < metricInterval.upper ? 
                                Truth.UNDEFINED : Truth.FALSE);
            }
        }
        else {  
            if (signal.isUndefined())
                this.truthValue[0] = Truth.UNDEFINED;
            else
                this.truthValue[0] = (signal.getValueAt(0) ? Truth.TRUE : Truth.FALSE);
        }
        */
        
    } 
    
    
    
    
}

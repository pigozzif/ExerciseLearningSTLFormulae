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
public class MTLglobally  extends MTLmodalNode {
    
    public MTLglobally(MTLnode child) {
        super(new ParametricInterval());
        //getParametricInterval().setUpperValue(100);
        super.setChild(child);
        child.setParent(this);
    }

    public MTLglobally(ParametricInterval interval, MTLnode child) {
        super(interval);
        super.setChild(child);
        child.setParent(this);
    }
    
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = child.duplicate();
        MTLnode n = new MTLglobally(n1);
        return n;
    }
    
    @Override
    public NodeType getType() {
        return NodeType.GLOBALLY;
    }
    
    @Override
    public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "Globally " + parametricInterval.toString() + "\n";
        return s + super.toFormulaTree(depth);
    }
    
//    @Override
//    public String toString() {
//        String s = "G" + parametricInterval.toString() + " (" + child.toString() + ")";
//        return s;
//    }

    @Override
    public String toString() {
        String s = "G" + parametricInterval.toString() + " (" + child.toString() + ") ";
        return s;
    }

    @Override
    public String toSign() {
        return child.toSign();
    }
    
    /*
     * Here we check in fact the formula eventually not phi, and negate the result
     */
    
     @Override
    boolean checkGoal(int j) {
        if (j < this.traceLength)
            //not sure here... what if undefined
            return !(child.truthValue[j] == Truth.TRUE);
        else return true; //not sure of this last point
    }

    @Override
    boolean checkSafe(int i) {
        return true;
    }

    @Override
    void setBad(int i) {
        this.truthValue[i] = Truth.TRUE;
    }

    @Override
    void setOk(int i) {
        this.truthValue[i] = Truth.FALSE;
    }
    
    
    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = BooleanSignalTransducer.always(child.signal, metricInterval.lower, metricInterval.upper);
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
                     this.truthValue[i] = BooleanSignalTransducer.alwaysZero(child.signal, metricInterval.lower, span[i]);                
            } else {
               for (int i=0;i<points;i++)
                     this.truthValue[i] = BooleanSignalTransducer.alwaysZero(child.signal, span[i], metricInterval.upper);                   
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
                //finds the first time instant T after T1 in G[T1,T2] in which the 
                //child formula is false. for any [T1,t] contained in [T1,T] the formula is true, 
                //provided it is true in T1
                TimeInterval I = child.signal.getFirstPositiveIntervalAfterT(metricInterval.lower);
                boolean included;
                double T;
                if (I == null) {
                    if (child.signal.getFinalTime() < metricInterval.lower) {
                        for (int i=0;i<points;i++) 
                            this.truthValue[i] = Truth.UNDEFINED;
                        return;
                    } else {
                        included = false;
                        T = child.signal.getFinalTime();
                    } 
                } else { 
                    included = I.contains(metricInterval.lower);
                    T = I.getUpper();
                }
                for (int i=0;i<points;i++) 
                    if (span[i] < T)
                        this.truthValue[i] = (included ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T)     
                        this.truthValue[i] = (included && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE);
                    else
                        this.truthValue[i] = (included && T < child.signal.getFinalTime() ? Truth.FALSE : Truth.UNDEFINED);
            } else {
                //finds the smallest T<=T2 such that  the child signal is true in [T,T2], if any.
                //the formula is true for each interval [t,T2] contained in [T,T2].
                TimeInterval I = child.signal.getLastPositiveIntervalBeforeT(metricInterval.upper);
                boolean included = I.contains(metricInterval.upper);
                double T = I.getLower();
                for (int i=0;i<points;i++) 
                    if (span[i] > T)
                        this.truthValue[i] = (included ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T)     
                        this.truthValue[i] = (included && I.isLowerClosed() ? Truth.TRUE : Truth.FALSE);
                    else
                        this.truthValue[i] = Truth.FALSE;
            }
        }
        else {   
            this.truthValue[0] = (signal.getValueAt(0) ? Truth.TRUE : Truth.FALSE);
        }
        */
        
    } 
    
}

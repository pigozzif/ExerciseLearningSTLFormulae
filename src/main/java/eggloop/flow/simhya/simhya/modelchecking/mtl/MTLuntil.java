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
public class MTLuntil  extends MTLmodalNode {
    
    public MTLuntil(MTLnode left, MTLnode right) {
        super(new ParametricInterval());
        getParametricInterval().setUpperValue(100);
        super.setChildren(left,right);
        left.setParent(this);
        right.setParent(this);
    }

    public MTLuntil(ParametricInterval interval, MTLnode left, MTLnode right) {
        super(interval);
        super.setChildren(left,right);
        left.setParent(this);
        right.setParent(this);
    }
    
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = left.duplicate();
        MTLnode n2 = right.duplicate();
        MTLnode n = new MTLuntil(n1,n2);
        return n;
    }
    
    
    @Override
    public NodeType getType() {
        return NodeType.UNTIL;
    }
    
    @Override
    public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "Until " + parametricInterval.toString() + "\n";
        return s + super.toFormulaTree(depth);
    }
    
    @Override
    boolean checkGoal(int j) {
        if (j < this.traceLength) 
            return right.truthValue[j] == Truth.TRUE;
        else return false;
    }

    @Override
    boolean checkSafe(int j) {
        if (j < this.traceLength) 
            return left.truthValue[j] == Truth.TRUE;
        else return false;
    }

    @Override
    void setBad(int i) {
        this.truthValue[i] = Truth.FALSE;
    }

    @Override
    void setOk(int i) {
        this.truthValue[i] = Truth.TRUE;
    }
   
    
//    @Override
//    public String toString() {
//        String s = "(" + left.toString() +  " U" + this.parametricInterval.toString() + " " + right.toString() + ")";
//        return s;
//    }


    @Override
    public String toString() {
        String s = "((" + left.toString() +  ") U" + this.parametricInterval.toString() + " (" + right.toString() + "))";
        return s;
    }

    @Override
    public String toSign() {
        return left.toSign()+""+right.toSign();
    }
    
    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        signal = BooleanSignalTransducer.until(left.signal, right.signal, metricInterval.lower, metricInterval.upper);
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
                     this.truthValue[i] = BooleanSignalTransducer.untilZero(left.signal,right.signal, metricInterval.lower, span[i]);                
            } else {
               for (int i=0;i<points;i++)
                     this.truthValue[i] = BooleanSignalTransducer.untilZero(left.signal,right.signal, span[i], metricInterval.upper);                   
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
            
            
            if (varyRightBound) {
                /*
                 * We want to check s1 U[T1,t] s2 at time zero, varying t in [T1,T2].
                 * We find the endpoint T0 of the positive interval of s1, if any, that contains 0.
                 * If T0 < T1, then the formula is false.
                 * Then we find the first positive interval after T1 for s2, and we let T be its lower bound
                 * We require T to be less than T0, otherwise the formula is false forall t.
                 * Then the formula is true for each [T1,t] with t >= T, with equality depending on T 
                 * being inclided or not in the signal interval.  
                 *
                TimeInterval I0 = left.signal.getFirstPositiveIntervalAfterT(0);
                boolean test = I0.contains(0);
                double T0 = I0.getUpper();
                test = test && ( T0 > metricInterval.lower || (T0 == metricInterval.lower && I0.isUpperClosed()) );
                TimeInterval I = right.signal.getFirstPositiveIntervalAfterT(metricInterval.lower);        
                double T = I.getLower();
                test = test && (T < T0 || (T == T0 && I.isLowerClosed() && I0.isUpperClosed() ));
                for (int i=0;i<points;i++) 
                    if (span[i] > T)
                        this.truthValue[i] = ( test ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T) 
                        this.truthValue[i] = (test && I.isLowerClosed() ? Truth.TRUE : Truth.FALSE);
                    else    
                        this.truthValue[i] = Truth.FALSE;
            } else {
                /*
                 * We want to check s1 U[t,T2] s2 at time zero, varying t in [T1,T2].
                 * We find the endpoint T0 of the positive interval of s1, if any, that contains 0.
                 * If T0 < T1, then the formula is false
                 * Else, if T0 >= T1, then we find the last positive interval for s2 that starts no
                 * later than T0. If this interval do not intersect [T1,T2], the formula is false
                 * Else, if it intersects it and if it also intersects [0,T0] of [0,T0[ (the final point depends on the
                 * status of the eight endpoint of the signal interval containing T0),
                 * then the formula with [t,T2] is true only if t<= min(T,T0), with equality requiring 
                 * appropriate endpoints to be included. 
                 *
                TimeInterval I0 = left.signal.getFirstPositiveIntervalAfterT(0);
                boolean test = I0.contains(0);
                double T0 = I0.getUpper();
                test = test && ( T0 > metricInterval.lower || (T0 == metricInterval.lower && I0.isUpperClosed()) );
                TimeInterval I = child.signal.getLastPositiveIntervalBeforeT(T0);
                test = test && (I.getLower() < T0 || (I.getLower() == T0 && I.isLowerClosed() && I0.isUpperClosed()) );
                test = test && (I.getUpper() > metricInterval.lower || (I.getUpper() == metricInterval.lower && I.isUpperClosed()) );
                double T = I.getUpper();      
                for (int i=0;i<points;i++) 
                    if (span[i] < T0 && span[i] < T)
                        this.truthValue[i] = ( test ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T0 && T0 < T)
                        this.truthValue[i] = (test && I0.isUpperClosed() ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T && T < T0)
                        this.truthValue[i] = (test && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T0 && T0 == T)
                        this.truthValue[i] = (test && I0.isUpperClosed() && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE);  
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignal;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignalTransducer;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.TimeInterval;

/**
 *
 * @author Luca
 */
public class MTLweakUntil  extends MTLmodalNode {
    
    public MTLweakUntil(MTLnode left, MTLnode right) {
        super(new ParametricInterval());
        super.setChildren(left,right);
        left.setParent(this);
        right.setParent(this);
    }

    public MTLweakUntil(ParametricInterval interval, MTLnode left, MTLnode right) {
        super(interval);
        super.setChildren(left,right);
        left.setParent(this);
        right.setParent(this);
    }
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = left.duplicate();
        MTLnode n2 = right.duplicate();
        MTLnode n = new MTLweakUntil(n1,n2);
        return n;
    }
    
    @Override
    public NodeType getType() {
        return NodeType.WUNTIL;
    }
    
    @Override
    public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "Weak Until " + parametricInterval.toString() + "\n";
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
    
        @Override
    void labelingFormulaPointwiseSemantics(double [] deltaTimes) {
        /*
         * In order to verify a modal node, different from next,
         * we need to verify if the formula holds in each state of the trace.
         * To do this, we need to find a path from i to j, such that from i to j-1,
         * states are safe; sate j is a goal state, and the time to go from i to j
         * satisfies the metric constrant of the model operator.
         * 
         * We do this backward on the trace, starting from the final state and then going down.
         * We let both i and j point at this final state, then we find the first state from the right 
         * that satisfies the metric bound (can be the last one).
         * 
         * The correctness of the following algorithm can be proved by showing the following invariant
         * that holds at each cycle iteration at point (*)
         * i >= 0 and there is a path of safe states from i to j excluded, and deltaT(i,j) in I
         * and all states from i+1 to n have been assigned the correct truth value.
         * 
         * The invariant is proved by induction, observing that the two while cicles assign the correct 
         * truth value. The second one because the invariant guarantees that all i marked ok are indeed so
         * and the first one set i correctly to bad because t is below the lower bound of the interval 
         * (this holds due to the last while loop) and i cannot reach a goal state in the right time
         * interval (we would have not reached this point with this pair of i-j)
         * When i is an unsafe state, then the formula is false in i, and no state can reach a goal state passing
         * through i, so we can safely move j over i.
         */
        int i = traceLength-1; 
        int j = traceLength-1; 
        boolean safeUntilEnd = checkSafe(i);
        //this keeps track of the duration from i to the length of the trace.
        double timeToEnd = 0;
        double DT = 0; 
        //DT is originally 0, as the last state is the final state ans has duration 0
        double DsafeT = 0;
        double [] safeT = new double[traceLength];
        //DsafeT stores the longest safe path from i, which can go beyond j. 
        while (i >= 0) {
            //here the next action depends on the relative position of DT w.r.t. the metric interval I
            RelativePosition pos = metricInterval.relativePositionOf(DT);
            if (pos == RelativePosition.ON_THE_LEFT) {
                //in this case, there is a good path from i to j (unless j<i),
                //but DT does not satisfy the metric constraint because it is two small. 
                //Hence, i is bad, as no state after j can make it a good state)
                if (timeToEnd < metricInterval.upper && safeUntilEnd)
                    setUndefined(i);
                else
                    setBad(i); //signals i as a bad state
                safeT[i] = DsafeT;
                i--;
                if (i >= 0) {
                    timeToEnd = updateTimeToEnd(timeToEnd, i+1, i, deltaTimes);
                    if (!checkSafe(i)) {
                        //if the new i state is not safe, then no path can pass through it, 
                        //hence we can move j over i
                        DT = updateDT(DT, i+1, i, j, i, deltaTimes);
                        j = i;
                        DsafeT = (i>0 ? -deltaTimes[i-1] : 0);
                        safeUntilEnd = false;
                    } else {//if i is safe, we just update DT
                        DT = updateDT(DT, i+1, i, j, j, deltaTimes);
                        DsafeT += deltaTimes[i];
                    }
                }
            } else if (pos == RelativePosition.ON_THE_RIGHT) {
                //here the metric bound is not fulfilled because DT is on the right
                //of I, hence j is too distant from i, and we need to move j left
                j--;
                DT = updateDT(DT, i, i, j+1, j, deltaTimes);
            } else if (pos == RelativePosition.OVERLAPS) {
                if (checkGoal(j)) { 
                    setOk(i);
                    safeT[i] = DsafeT;
                    i--;
                    if (i >= 0) {
                        timeToEnd = updateTimeToEnd(timeToEnd, i+1, i, deltaTimes);
                        if (!checkSafe(i)) {
                            //if the new i state is not safe, then no path can pass through it, 
                            //hence we can move j over i
                            DT = updateDT(DT, i+1, i, j, i, deltaTimes);
                            j = i;
                            DsafeT = (i>0 ? -deltaTimes[i-1] : 0);
                            safeUntilEnd = false;
                        } else {//if i is safe, we just update DT
                            DT = updateDT(DT, i+1, i, j, j, deltaTimes);
                            DsafeT += deltaTimes[i];
                        }
                    }
                } else {
                    //if j is not a goal state, then we can move it on the left
                    j--;
                    DT = updateDT(DT, i, i, j+1, j, deltaTimes);
                }
            }
        }
        //this should correct for weak semantics of until, by setting as good
        //all those paths from which there is a safe path longer than the metric bound. 
        //notice that infinite weak until always fails in this semantics, that terminates at a finite time.
        //NOTICE, the condition on undefined seems redundant
        for (int k=0;k<traceLength;k++) {
            if (safeT[k] >= metricInterval.upper && this.truthValue[k] != Truth.UNDEFINED)
                this.truthValue[k] = Truth.TRUE;
            //set to true if there is a safe path longer than the metric bound
        }
            
    }
    
    @Override
    void labelingTopFormulaPointwiseSemantics(double [] deltaTimes, 
            boolean varyInterval, boolean varyRightBound, int points) {
        if (varyInterval && !metricInterval.upperIsInfinity &&
                metricInterval.lower < metricInterval.upper && points > 1) {
            //compute the span points
            double delta = (metricInterval.upper - metricInterval.lower)/(points-1);
            double [] span = new double[points];
                for (int i=0;i<points;i++)
                    span[i] = metricInterval.lower + i*delta;
            Interval interval = new Interval();
            if (varyRightBound) {
                interval.setLower(metricInterval.lower, metricInterval.lowerClosed);
                int j = 0;
                double t = 0; //time at state j in trace
                for (int k=0;k<points;k++) {
                    interval.setUpper(span[k], metricInterval.upperClosed);
                    while (true) {
                        if (j >= super.traceLength) {
                            //we have not found a goal state and we overcome the interval
                            //we can signal as bad all states, as there is no chance of
                            //making the formula true
                            for ( ;k<points;k++)
                                //setBad(k);
                                setUndefined(k);
                             break;
                        }
                        if (interval.contains(t) && checkGoal(j)) {
                            //if we enter the metric bound and we find a goal state, then 
                            //we set all remaining points to true
                            for ( ;k<points;k++)
                                setOk(k);
                            break;
                        } else if (interval.upperBoundSmallerThan(t)) {
                            //we have not found a goal state and we overcome the interval
                            //so k is good, according to the weak semantics of until
                            setOk(k); 
                            break;
                        } 
                        //if we are here, we need to check if the system is safe
                        if (checkSafe(j)) {
                            //if we find not a goal but a safe state, we advance to next state
                            t += deltaTimes[j];
                            j++;
                        } else {
                            //if we enter an unsafe state then we set all states as bad
                             for ( ;k<points;k++)
                                setBad(k);
                             break;
                        }
                    }
                }
            } else {
                interval.setUpper(metricInterval.upper, metricInterval.upperClosed);
                int j = 0;
                double t = 0; //time at state j in trace
                for (int k=0;k<points;k++) {
                    interval.setLower(span[k], metricInterval.lowerClosed);
                    while (true) {
                        if (j >= super.traceLength) {
                            //we have not found a goal state and we overcome the interval
                            //we can signal as bad all states, as there is no chance of
                            //making the formula true
                            for ( ;k<points;k++)
                                //setBad(k);
                                setUndefined(k);
                             break;
                        }
                        if (interval.contains(t) && checkGoal(j)) {
                            //if we enter the metric bound and we find a goal state, then 
                            //we set the current k to ok and we continue.
                            setOk(k);
                            break;
                        } else if (interval.upperBoundSmallerThan(t)) {
                            //we have not found a goal state and we overcome the interval
                            //we can signal as good all states
                            for ( ;k<points;k++)
                                setOk(k);
                             break;
                        } 
                        //if we are here, we need to check if the system is safe
                        if (checkSafe(j)) {
                            //if we find not a goal but a safe state, we advance to next state
                            t += deltaTimes[j];
                            j++;
                        } else {
                            //if we enter an unsafe state then we set all states as bad
                             for ( ;k<points;k++)
                                setBad(k);
                             break;
                        }
                    }
                }
            }
        } else {
            //in this case, we just need to aswer yes or no
            int j = 0;
            double t = 0; //time at state j in trace
            while (true) {
                if (j >= super.traceLength) {
                    //we have not found a goal state and we finished the trace, hence
                    //the trace is bad -> I guess undefined
                    setUndefined(0);
                    //setBad(0); 
                    break;
                }
                if (metricInterval.contains(t) && checkGoal(j)) {
                    //if we enter the metric bound and we find a goal state, then 
                    //we set all remaining points to true
                    setOk(0);
                    break;
                } else if (metricInterval.upperBoundSmallerThan(t)) {
                    //we have not found a goal state and we overcome the interval
                    //so the trace is bad
                    setOk(0); 
                    break;
                } 
                //if we are here, we need to check if the system is safe
                if (checkSafe(j)) {
                    //if we find not a goal but a safe state, we advance to next state
                    t += deltaTimes[j];
                    j++;
                } else {
                    //if we enter an unsafe state then we set the trace as bad
                     setBad(0);
                     break;
                }
            }
        }
    }

    @Override
    void labelingFormulaSignalSemantics(double t0, double tf) {
        //phi1 W[t1,t2] phi2 ~ G[0,t2] phi1 OR phi1 U[t1,t2] phi2
        BooleanSignal s1 = BooleanSignalTransducer.always(left.signal, 0, metricInterval.upper);
        BooleanSignal s2 = BooleanSignalTransducer.until(left.signal, right.signal, metricInterval.lower, metricInterval.upper);        
        signal = BooleanSignalTransducer.or(s1, s2);                        
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
                for (int i=0;i<points;i++) {
                     this.truthValue[i] = BooleanSignalTransducer.untilZero(left.signal,right.signal, metricInterval.lower, span[i]);
                     this.truthValue[i] = Truth.or(truthValue[i], BooleanSignalTransducer.alwaysZero(left.signal, left.signal.getInitialTime(), span[i]));
                }
            } else {
               for (int i=0;i<points;i++) {
                     this.truthValue[i] = BooleanSignalTransducer.untilZero(left.signal,right.signal, span[i], metricInterval.upper);
                     this.truthValue[i] = Truth.or(truthValue[i], BooleanSignalTransducer.alwaysZero(left.signal, left.signal.getInitialTime(), metricInterval.upper));
               }
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
                //finds the first time instant T after T1 in G[T1,T2] in which the 
                //child formula is false. for any [T1,t] contained in [T1,T] the formula is true, 
                //provided it is true in T1
                TimeInterval I = child.signal.getFirstPositiveIntervalAfterT(metricInterval.lower);
                boolean included = I.contains(metricInterval.lower);
                double T = I.getUpper();
                for (int i=0;i<points;i++) 
                    if (span[i] < T)
                        this.truthValue[i] = (included ? Truth.TRUE : Truth.FALSE);
                    else if (span[i] == T)     
                        this.truthValue[i] = (included && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE);
                    else
                        this.truthValue[i] = Truth.FALSE;
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
                        this.truthValue[i] = Truth.or(this.truthValue[i], (test ? Truth.TRUE : Truth.FALSE));
                    else if (span[i] == T) 
                        this.truthValue[i] = Truth.or(this.truthValue[i], (test && I.isLowerClosed() ? Truth.TRUE : Truth.FALSE));
                    else    
                        this.truthValue[i] = Truth.or(this.truthValue[i], Truth.FALSE);
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
                        this.truthValue[i] = Truth.or(this.truthValue[i], ( test ? Truth.TRUE : Truth.FALSE));
                    else if (span[i] == T0 && T0 < T)
                        this.truthValue[i] = Truth.or(this.truthValue[i], (test && I0.isUpperClosed() ? Truth.TRUE : Truth.FALSE));
                    else if (span[i] == T && T < T0)
                        this.truthValue[i] = Truth.or(this.truthValue[i], (test && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE));
                    else if (span[i] == T0 && T0 == T)
                        this.truthValue[i] = Truth.or(this.truthValue[i], (test && I0.isUpperClosed() && I.isUpperClosed() ? Truth.TRUE : Truth.FALSE));  
                    else    
                        this.truthValue[i] = Truth.or(this.truthValue[i], Truth.FALSE);      
            }
            
   
        } else {   
            this.truthValue[0] = (signal.getValueAt(0) ? Truth.TRUE : Truth.FALSE);
        }
         * 
         * 
         */
    }
    
    
    
    
    
    
    
    @Override
    public String toString() {
        String s = "(" + left.toString() +  " W" + this.parametricInterval.toString() + " " + right.toString() + ")";
        return s;
    }
    @Override
    public String toSign() {
        return"";
    }

    

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

import java.util.ArrayList;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;

/**
 *
 * @author Luca
 */
public abstract class MTLmodalNode  extends MTLnode {
    ParametricInterval parametricInterval;
    Interval metricInterval;

    public MTLmodalNode(ParametricInterval interval) {
        super();
        this.parametricInterval = interval;
    }
    
    
    public void setParametricInterval(ParametricInterval interval) {
        this.parametricInterval = interval;
    }
    
    public ParametricInterval getParametricInterval() {
        return this.parametricInterval;
    }

    @Override
    int countTemporalNodes() {
        return super.countTemporalNodes() + 1;
    }
    
    
    
    
    
    
    @Override
    public void recomputeBounds(SymbolArray localSymbols) {
        metricInterval = new Interval();
        String l = parametricInterval.getLowerParameter();
        String u = parametricInterval.getUpperParameter();
        if (l != null) {
            int id = localSymbols.getSymbolId(l);
            double x = localSymbols.getValue(id);
            metricInterval.setLower(x, true);
        } else {
            metricInterval.setLower(parametricInterval.getLower(), true);
        }
        if (u != null) {
            int id = localSymbols.getSymbolId(u);
            double x = localSymbols.getValue(id);
            metricInterval.setUpper(x, true);
        } else if (parametricInterval.isUpperInfinity()) {
             metricInterval.setUpperToInfinity();
        } else {
            metricInterval.setUpper(parametricInterval.getUpper(), true);  ;
        }
        super.recomputeBounds(localSymbols);
    }

    @Override
    boolean containsParameter(String param) {
        boolean a =  super.containsParameter(param);
        a = a || this.parametricInterval.contains(param);
        return a;
    }
    
   
    
    
    @Override
    void setTopNodeInfo(boolean belowModalNode) {
        isTopNode = !belowModalNode;
        if (!belowModalNode)
            belowModalNode = true;
        if (left != null) 
            left.setTopNodeInfo(belowModalNode);
        if (right != null)
            right.setTopNodeInfo(belowModalNode);
        if (child != null)
            child.setTopNodeInfo(belowModalNode);
    }

    @Override
    ParametricInterval setExplorableTopNodes(String parameter) {
        //unbounded intervals cannot be explored.
        if (this.parametricInterval.upperIsInfinity) {
            ParametricInterval pint = new ParametricInterval();
            pint.setUndefined();
            return pint;
        }    //we now check if the interval contains the parameter. Any interval contains a null parameter.
        else if(this.parametricInterval.contains(parameter)) {
            return this.parametricInterval;
        } else {
            ParametricInterval pint = new ParametricInterval();
            pint.setUndefined();
            return pint;
        }
    }

    @Override
    void setExplorableTopNodes(String parameter, boolean value) {
        if (this.isTopNode) {
            if (this.parametricInterval.upperIsInfinity)
                this.isExplorableTopNode = false;
            else if (parametricInterval.contains(parameter))
                this.isExplorableTopNode = value;
            else 
                this.isExplorableTopNode = false;
        }
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
        while (i >= 0) {
            //here the next action depends on the relative position of DT w.r.t. the metric interval I
            RelativePosition pos = metricInterval.relativePositionOf(DT);
            if (pos == RelativePosition.ON_THE_LEFT) {
                //in this case, there is a good path from i to j (unless j<i),
                //but DT does not satisfy the metric constraint because it is too small. 
                //Hence, i is bad, as no state after j can make it a good state)
                //However, if the trace is not long enough, but it is safe until the end, then we do not have
                //enough information to establish if the formula is true or false, and we set its status
                //to undefined.
                if (timeToEnd < metricInterval.upper && safeUntilEnd)
                    setUndefined(i);
                else
                    setBad(i); //signals i as a bad state
                i--;
                if (i >= 0) {
                    timeToEnd = updateTimeToEnd(timeToEnd, i+1, i, deltaTimes);
                    if (!checkSafe(i)) {
                        //if the new i state is not safe, then no path can pass through it, 
                        //hence we can move j over i
                        DT = updateDT(DT, i+1, i, j, i, deltaTimes);
                        j = i;
                        safeUntilEnd = false;
                    } else //if i is safe, we just update DT
                        DT = updateDT(DT, i+1, i, j, j, deltaTimes);
                }
            } else if (pos == RelativePosition.ON_THE_RIGHT) {
                //here the metric bound is not fulfilled because DT is on the right
                //of I, hence j is too distant from i, and we need to move j left
                j--;
                DT = updateDT(DT, i, i, j+1, j, deltaTimes);
            } else if (pos == RelativePosition.OVERLAPS) {
                if (checkGoal(j)) { 
                    setOk(i);
                    i--;
                    if (i >= 0) {
                        timeToEnd = updateTimeToEnd(timeToEnd, i+1, i, deltaTimes);
                        if (!checkSafe(i)) {
                            //if the new i state is not safe, then no path can pass through it, 
                            //hence we can move j over i
                            DT = updateDT(DT, i+1, i, j, i, deltaTimes);
                            j = i;
                            safeUntilEnd = false;
                        } else //if i is safe, we just update DT
                            DT = updateDT(DT, i+1, i, j, j, deltaTimes);
                    }
                } else {
                    //if j is not a goal state, then we can move it on the left
                    j--;
                    DT = updateDT(DT, i, i, j+1, j, deltaTimes);
                }
            }
        }
    }
    
    /**
     * This method updates the time difference between state i and state j.
     * Assuming the pointwise semantics, if it has the trace semantics
     * or the continuous semantics. Only one index between i and j changes, and decreases by one
     * @param DT the current time difference interval
     * @param i the old i index
     * @param iprime the new i index
     * @param j the old j index
     * @param jprime the new j index
     * @param deltaTimes the time difference between two states
     * @param semantics the semantic interpretation of MTL 
     */
    double updateDT(double DT, int i, int iprime, int j, int jprime, double [] deltaTimes) {
            if (i != iprime) { //i changed, from i to i-1
                if (iprime == jprime) {
                    //if j' = i, then DT = 0
                    return 0;
                } else {
                    return DT + deltaTimes[iprime];
                    //here we add dt(i')
                }
            } else if (j != jprime ) {
                if (iprime == jprime) {
                    //if j' = i, then DT = 0
                    return 0;
                } else {
                    //here we subtract dt(j')
                    if (jprime >= 0) {
                        return DT - deltaTimes[jprime];
                    } else {
                        return Double.NEGATIVE_INFINITY;
                    }
                }   
            }
            return 0;
    }
    
    double updateTimeToEnd(double TTE, int i, int iprime, double [] deltaTimes) {
            if (i != iprime) { //i changed, from i to i-1
                    return TTE + deltaTimes[iprime];
                    //here we add dt(i')
            } else 
                return TTE;
    }

    @Override
    boolean checkConsistencyOfTimeBounds() {
        boolean truth = super.checkConsistencyOfTimeBounds();
        truth = truth && this.metricInterval.checkConsistency();
        return truth;
    }
    
    
    
    
//    /**
//     * This method updates the time difference between state i and state j.
//     * The result of this method depends on the semantics of MTL, if it has the trace semantics
//     * or the continuous semantics. Only one index between i and j changes, and decreases by one
//     * @param DT the current time difference interval
//     * @param i the old i index
//     * @param iprime the new i index
//     * @param j the old j index
//     * @param jprime the new j index
//     * @param deltaTimes the time difference between two states
//     * @param semantics the semantic interpretation of MTL 
//     */
//    private void updateDT(Interval DT, int i, int iprime, int j, int jprime, 
//            double [] deltaTimes, SemanticInterpretation semantics) {
//        if (semantics == SemanticInterpretation.CONTINUOUS_SEMANTICS) {
//            if (i != iprime) { //i changed, from i to i-1
//                if (iprime == jprime) {
//                    //if i' and j' are in the same position, then
//                    // DT' = [0,dt(i')[
//                    DT.setLower(0, true);
//                    DT.setUpper(deltaTimes[iprime], false);
//                } else if (i == j) {
//                    //if i == j, then i' = j-1, and DT' = ]0,dt(i')+dt(i)[
//                    DT.setLower(0, false);
//                    DT.setUpper(deltaTimes[iprime]+deltaTimes[i], false);
//                } else if (i < j) {
//                    //in this case DT' = ]DT1+dt(i),DT2+dt(i')[
//                    DT.setLower(DT.lower + deltaTimes[i], false);
//                    DT.setUpper(DT.upper + deltaTimes[iprime], false);
//                }
//            } else if (j != jprime) { //j changed
//                if (jprime == iprime) {
//                    //in this case DT' = [0,dt(i')[
//                    DT.setLower(0, true);
//                    DT.setUpper(deltaTimes[iprime], false);
//                } else if (jprime == iprime-1) {
//                    //in this case, DT = ]dt(i')+dt(j'),0[
//                    if (jprime >= 0)
//                        DT.setLower(-deltaTimes[iprime]-deltaTimes[jprime], false);
//                    else 
//                        DT.setLower(Double.NEGATIVE_INFINITY, false);
//                    DT.setUpper(0, false);
//                } else if (iprime < jprime) {
//                    //in this case, DT = ]DT1 - dt(j'),DT2 - dt(j)[
//                    DT.setLower(DT.lower - deltaTimes[jprime], false);
//                    DT.setUpper(DT.upper - deltaTimes[j], false);
//                }
//            }   
//        } else if (semantics == SemanticInterpretation.TRACE_SEMANTICS) {
//            if (i != iprime) { //i changed, from i to i-1
//                if (iprime == jprime) {
//                    //if j' = i, then DT = 0
//                    DT.setLower(0, true);
//                    DT.setUpper(0, true);
//                } else {
//                    //here we add dt(i')
//                    DT.setLower(DT.lower + deltaTimes[iprime], true);
//                    DT.setUpper(DT.upper + deltaTimes[iprime], true);
//                }
//            } else if (j != jprime ) {
//                if (iprime == jprime) {
//                    //if j' = i, then DT = 0
//                    DT.setLower(0, true);
//                    DT.setUpper(0, true);
//                } else {
//                    //here we subtract dt(j')
//                    if (jprime >= 0) {
//                        DT.setLower(DT.lower - deltaTimes[jprime], true);
//                        DT.setUpper(DT.upper - deltaTimes[jprime], true);
//                    } else {
//                        DT.setLower(Double.NEGATIVE_INFINITY, true);
//                        DT.setUpper(Double.NEGATIVE_INFINITY, false);
//                    }
//                }   
//            }
//            
//            
//        }
//    }
//    

    
    
    
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
                            //we have not found a goal state and we finished the trajectory, 
                            //all is undefined, as there is no chance of making the formula true
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
                            //so k is bad, and we need to increase the right extreme 
                            //of the interval
                            setBad(k); 
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
                            //we can signal as bad all states, as there is no chance of
                            //making the formula true
                            for ( ;k<points;k++)
                                setBad(k);
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
                    setBad(0); 
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
     
    
    /**
     * checks if state j is a goal state for the temporal operator
     * @param j
     * @return 
     */
    abstract boolean checkGoal(int j);
    /**
     * Check if state i is a safe state for temporal operator
     * @param i
     * @return 
     */
    abstract boolean checkSafe(int i);
    
    /**
     * sets the state i to ok for the reachability (the meaning of ok is 
     * reversed for universal quantification)
     * @param i 
     */
    abstract void setOk(int i);
    /**
     * sets the state i to bad for the reachability (the meaning of ok is 
     * reversed for universal quantification)
     * @param i 
     */
    abstract void setBad(int i);
    
    /**
     * sets the truth value of state i to undefined
     * @param i 
     */
    void setUndefined(int i) {
        this.truthValue[i] = Truth.UNDEFINED;
    }

    @Override
    public MTLnode changeType(NodeType newtype, boolean keepLeft, MTLnode newLeft, MTLnode newRight, ParametricInterval interval) {
        MTLnode n = super.changeType(newtype, keepLeft, newLeft, newRight, interval);
        if (n instanceof MTLmodalNode) {
            MTLmodalNode z = (MTLmodalNode)n;
            if (interval != null)
                z.parametricInterval = interval;
            else
                z.parametricInterval = this.parametricInterval;
        }
        return n;
    }

    @Override
    public ArrayList<String> getParameters(ArrayList<String> list) {
        list = super.getParameters(list);
        if  (this.parametricInterval.lowerParameter != null)
            list.add(this.parametricInterval.lowerParameter);
        if  (this.parametricInterval.upperParameter != null)
            list.add(this.parametricInterval.upperParameter);
        return list;
    }

    @Override
    public double computeTimeDepth() {
        double T = super.computeTimeDepth();
        if (this.metricInterval != null) 
            T += metricInterval.upper;
        return T;
    }
    
    
    
    
    
    
    
}

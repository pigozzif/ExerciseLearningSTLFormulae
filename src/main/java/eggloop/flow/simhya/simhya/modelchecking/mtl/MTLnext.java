/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;


/**
 *
 * @author Luca
 */
public class MTLnext  extends MTLmodalNode {

    public MTLnext(MTLnode child) {
        super(new ParametricInterval());
        super.setChild(child);
        child.setParent(this);
    }
    
    public MTLnext(ParametricInterval interval, MTLnode child) {
        super(interval);
        super.setChild(child);
        child.setParent(this);
    }
    
    @Override
    public MTLnode duplicate() {
        MTLnode n1 = child.duplicate();
        MTLnode n = new MTLnext(n1);
        return n;
    }
    
    
    @Override
    public NodeType getType() {
        return NodeType.NEXT;
    }
    
    @Override
    public String toFormulaTree(int depth) {
        String s = "";
        for (int i=0;i<depth;i++)
            s += "   ";
        s += "Next " + parametricInterval.toString() + "\n";
        return s + super.toFormulaTree(depth);
    }
    
    @Override
    public String toString() {
        String s = "X" + parametricInterval.toString() + " (" + child.toString() + ")"; 
        return s;
    }

    @Override
    public String toSign() {
        return"";
    }

    
    @Override
    void labelingFormulaPointwiseSemantics(double[] deltaTimes) {
        for (int i=0;i<this.traceLength-1;i++) {
            //we cannot establish the truth of next for the last state!
            if ( checkGoal(i+1) && metricInterval.contains(deltaTimes[i]))
                setOk(i);
            else 
                setBad(i);
        }
        //last state is undefined
        setUndefined(this.traceLength-1);
    }

    @Override
    void labelingTopFormulaPointwiseSemantics(double[] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {
        if (this.traceLength == 1) {
            for (int k=0;k<points;k++)
                setUndefined(k);
                return;
        } 
        double t = deltaTimes[0]; //time at state 1 in trace
        if (varyInterval && !metricInterval.upperIsInfinity &&
                metricInterval.lower < metricInterval.upper && points > 1) {
            //compute the span points
            double delta = (metricInterval.upper - metricInterval.lower)/(points-1);
            double [] span = new double[points];
                for (int i=0;i<points;i++)
                    span[i] = metricInterval.lower + i*delta;
            if (varyRightBound) {
                if (!checkGoal(1) || t < metricInterval.lower || t > metricInterval.upper) {
                    //either 1 is not a goal state, or the time elapsed is not enough
                    for (int k=0;k<points;k++)
                        setBad(k);
                } else {
                    //if state 1 is a goal state, and time 
                    for (int k=0;k<points;k++) {
                        if (t <= span[k]) {
                            for ( ;k<points;k++)
                                setOk(k);
                            break;
                        } else
                            setBad(k);
                    }
                }
            } else {
                if (!checkGoal(1) || t < metricInterval.lower || t > metricInterval.upper) {
                    //either 1 is not a goal state, or the time elapsed is not enough
                    for (int k=0;k<points;k++)
                        setBad(k);
                } else {
                    //if state 1 is a goal state, 
                    for (int k=0;k<points;k++) {
                        if (t < span[k]) {
                            //here time is out the interval.
                            for ( ;k<points;k++)
                                setBad(k);
                            break;
                        } else
                            setOk(k);
                    }
                }
            }
        } else {
            //in this case, we just need to aswer yes or no
            if (!checkGoal(1) || t < metricInterval.lower || t > metricInterval.upper)
                setBad(0);
            else setOk(0);
        }
    }

    @Override
    boolean checkGoal(int j) {
        if (j < this.traceLength) 
            return child.truthValue[j] == Truth.TRUE;
        else return false;
    }

    @Override
    boolean checkSafe(int i) {
        return false;
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
        throw new UnsupportedOperationException("Boolean signal semantics not supported for Next operator.");
    }

    @Override
    void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        throw new UnsupportedOperationException("Boolean signal semantics not supported for Next operator.");
    }
    
    
   
}

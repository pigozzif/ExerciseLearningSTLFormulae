/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

import eggloop.flow.simhya.simhya.modelchecking.Formula;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import java.util.HashMap;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.dataprocessing.Trajectory;
import eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import eggloop.flow.simhya.simhya.modelchecking.*;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignal;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignalTransducer;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;


/**
 *
 * @author Luca
 */
public class MTLformula implements Formula {
    MTLnode root;
    Threshold threshold;
    ArrayList<MTLatom> atomicNodes;
    Store store;
    String name;



    private boolean varyInterval;
    private boolean varyRightBound;
    private int points;
    private int storeVars;
    private SMCtest tester = null;
    private SMCestimateTrajectory estimator = null;
    private SMCcontroller controller = null;
    private boolean estimate;
    private boolean ready = false;
    private boolean consistentTimeBounds = true;
    private boolean piecewiseLinearSignals = false;

    public MTLformula(MTLnode root) {
        this.root = root;
        this.threshold = null;
        atomicNodes = new ArrayList<MTLatom>();
        root.collectAtomicNodes(atomicNodes);
        estimate = true;
        root.setTopNodeInfo();
    }
    public MTLnode getRoot(){
        return root;
    }

    public MTLformula(Threshold threshold, MTLnode root) {
        this.threshold = threshold;
        this.root = root;
        atomicNodes = new ArrayList<MTLatom>();
        root.collectAtomicNodes(atomicNodes);
        estimate = false;
        root.setTopNodeInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    
    public void setPiecewiseLinearSignals() {
        this.piecewiseLinearSignals = true;
    }
    
    public void setPiecewiseConstantSignals() {
        this.piecewiseLinearSignals = false;
    }
    

    public void initalize(Store store, SymbolArray localSymbols, HashMap predicatesToExpression) {
        this.store = store;
        this.storeVars  = store.getNumberOfVariables();
        root.initalize(store,localSymbols,predicatesToExpression);
        root.recomputeBounds(localSymbols);
    }
    
    /**
     * Refreshes the information within the formula. To be called after any modification of formula
     * structure.
     */
    public void refresh() {
        atomicNodes = new ArrayList<MTLatom>();
        root.collectAtomicNodes(atomicNodes);
        root.setTopNodeInfo();
    }

    public void recomputeBounds(SymbolArray localSymbols) {
        root.recomputeBounds(localSymbols);
        if (threshold != null) {
            String s = threshold.getThresholdParameter();
            if (s != null) {
                int id = localSymbols.getSymbolId(s);
                double x = localSymbols.getValue(id);
                threshold.setThresholdValue(x);
            }
        }
    }
    
    public double getTimeDepth() {
        return root.computeTimeDepth();
    }

    public String toFormulaTree() {
        String s = "";
        s += "Probability " + (threshold == null ? "=?" : threshold.toString()) + "\n";
        s += root.toFormulaTree(1);
        return s;
    }

    
    
    @Override
    public String toString() {
        return root.toString();
    }

    public String toSign() {
        return root.toSign();
    }

    
    
    public ArrayList<Plot2DTrajectory> getSignals() {
        return root.getSignals();
    } 
    
    
    
    /**
     * This initializes the formula for estimation.
     * @param estimator
     * @param parameter
     * @param varyInterval
     * @param varyRightBound
     * @param points 
     */
    public void initializeForEstimate(SMCestimateTrajectory estimator, String parameter, 
            boolean varyInterval, boolean varyRightBound, int points) {
        if (!this.estimate)
            throw new RuntimeException("This formula doea not support estimation, initialize it for testing");
        this.varyInterval = varyInterval;
        this.varyRightBound = varyRightBound;
        this.points = points;
        this.root.testAndSetExplorableTopNodes(parameter);
        this.estimator = estimator;
        this.controller = estimator;
        this.ready = true;
        this.consistentTimeBounds = this.checkConsistencyOfTimeBounds();
        
    }
    
    
    public boolean containsParameter(String param) {
        return root.containsParameter(param);
    }
    
    /**
     * This initializes the formula for estimation.
     * @param estimator
     * @param parameter
     * @param varyInterval
     * @param varyRightBound
     * @param points 
     */
    public void initializeForEstimate(SMCestimateTrajectory estimator) {
        if (!this.estimate)
            throw new RuntimeException("This formula doea not support estimation, initialize it for testing");
        this.varyInterval = false;
        this.varyRightBound = false;
        this.points = 1;
        this.consistentTimeBounds = this.checkConsistencyOfTimeBounds();
        this.estimator = estimator;
        this.controller = estimator;
        this.ready = true;
    }
    
    
    /**
     * This initializes the formula for model checking with testing.
     */
    public void initializeForModelChecking(SMCtest tester) {
        if (this.estimate)
            throw new RuntimeException("This formula doea not support testing, initialize it for estimation");
        this.tester = tester;
        this.controller = tester;
        this.varyInterval = false;
        this.varyRightBound = true;
        this.points = 1;
        this.ready = true;
        this.consistentTimeBounds = this.checkConsistencyOfTimeBounds();
    }
    
    /**
     * this initialises the formula with a basic estimator
     */
    public void initializeForEstimate(SMCcontroller estimator) {
        if (!this.estimate)
            throw new RuntimeException("This formula doea not support estimation, initialize it for testing");
        this.tester = null;
        this.controller = estimator;
        this.varyInterval = false;
        this.varyRightBound = false;
        this.points = 1;
        this.ready = true;
        this.consistentTimeBounds = this.checkConsistencyOfTimeBounds();
    }

    
    public boolean estimate() {
        return this.estimate;
    }
    
    public Threshold getThreshold() {
        return this.threshold;
    }
    
    boolean checkConsistencyWithTimeBounds(double T) {
        double Tbound = root.computeTimeDepth();
        return T >= Tbound;
    }
    
    
    boolean checkConsistencyOfTimeBounds() {
        return root.checkConsistencyOfTimeBounds();
    }
    
    
    /**
     * this is the key method to call in a routine to model check a formula.
     * A simulator has to be used to feed trajectories to the method, one by one. 
     * Then each trajectory is model checked and the proper action is performed in 
     * terms of estimate or testing.
     * The method returns true if and only if the model checking procedure has reached
     * an end, according to current accuracy criteria.
     * @param traj
     * @return 
     */
    public boolean modelCheckNextTrajectoryPointwiseSemantics(Trajectory traj) {
        //formula is not ready to be model checked. 
        if (!ready)
            //return true;
            throw new RuntimeException("Formula not ready to be model checked. Either model checking "
                    + "has not been initialized or it terminated already");
        boolean answer;
        if (this.consistentTimeBounds) {
            //we init nodes
            this.root.initForModelCheckingPointwiseSemantics(points, traj.getPoints());
            //check consistency with time bounds here???
            //we compute truth value of atomic formulae
            this.setAtomicTruthValuePointwiseSemantics(traj);
            double [] deltaTimes = computeTimeIncrements(traj);
            Truth [] truth = root.modelCheckPointwiseSemantics(deltaTimes, varyInterval, varyRightBound, points);
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        else {
            Truth[] truth = new Truth[1];
            truth[0] = Truth.UNDEFINED;
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        return answer;
    }
    
    
    
    /**
     * Model checking starting from a double [][], whose first row is time
     * @param traj
     * @return 
     */
     public boolean modelCheckNextTrajectoryPointwiseSemantics(double[][] traj) {
        //formula is not ready to be model checked. 
        if (!ready)
            //return true;
            throw new RuntimeException("Formula not ready to be model checked. Either model checking "
                    + "has not been initialized or it terminated already");
        //we init nodes, first atomic nodes, then the rest!
        boolean answer;
        if (this.consistentTimeBounds) {
            this.root.initForModelCheckingPointwiseSemantics(points, traj[0].length);
            //we compute truth value of atomic formulae
            this.setAtomicTruthValuePointwiseSemantics(traj);
            double [] deltaTimes = computeTimeIncrements(traj);
            Truth [] truth = root.modelCheckPointwiseSemantics(deltaTimes, varyInterval, varyRightBound, points);
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        else {
            Truth[] truth = new Truth[1];
            truth[0] = Truth.UNDEFINED;
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        return answer;
    }
    
     
     /**
     * this is the key method to call in a routine to model check a formula.
     * A simulator has to be used to feed trajectories to the method, one by one. 
     * Then each trajectory is model checked and the proper action is performed in 
     * terms of estimate or testing.
     * The method returns true if and only if the model checking procedure has reached
     * an end, according to current accuracy criteria.
     * @param traj
     * @return 
     */
    public boolean modelCheckNextTrajectorySignalSemantics(Trajectory traj) {
        //formula is not ready to be model checked. 
        if (!ready)
            //return true;
            throw new RuntimeException("Formula not ready to be model checked. Either model checking "
                    + "has not been initialized or it terminated already");
        boolean answer;
        if (this.consistentTimeBounds) {
            //we init nodes
            this.root.initForModelCheckingSignalSemantics(points); 
            //check consistency with time bounds here???
            //we compute truth value of atomic formulae
            this.setAtomicTruthValueBooleanSemantics(traj);
            double t0 = traj.getData(0, 0);
            double tf = traj.getData(0, traj.getPoints()-1);
            Truth [] truth = root.modelCheckSignalSemantics(t0, tf, varyInterval, varyRightBound, points); 
            controller.addPoint(truth);
            
//            if (truth[0] == Truth.UNDEFINED) {
//                
//                root.printSignals();   
//                this.setAtomicTruthValueBooleanSemantics(traj);
//            }
            
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        else {
            Truth[] truth = new Truth[1];
            truth[0] = Truth.UNDEFINED;
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        return answer;
    }
     
     /**
     * Model checking starting from a double [][], whose first row is time
     * @param traj
     * @return 
     */
     public boolean modelCheckNextTrajectorySignalSemantics(double[][] traj) {
        //formula is not ready to be model checked. 
        if (!ready)
            //return true;
            throw new RuntimeException("Formula not ready to be model checked. Either model checking "
                    + "has not been initialized or it terminated already");
        //we init nodes, first atomic nodes, then the rest!
        boolean answer;
        if (this.consistentTimeBounds) {
            this.root.initForModelCheckingSignalSemantics(points); 
            //we compute truth value of atomic formulae
            this.setAtomicTruthValueBooleanSemantics(traj);
            double t0 = traj[0][0];
            double tf = traj[0][traj.length-1];
            Truth [] truth = root.modelCheckSignalSemantics(t0,tf, varyInterval, varyRightBound, points);
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        else {
            Truth[] truth = new Truth[1];
            truth[0] = Truth.UNDEFINED;
            controller.addPoint(truth);
            answer = controller.stop();
            if (answer) {
                ready = false;
                controller.finalization();
            }
        }
        return answer;
    }
     
     
     public void storeSignalInController() {
         controller.setSignals(this.getSignals());
     }
    
    /**
     * Computes the time increments for each state of the trajectory
     * @param traj
     * @return 
     */
    private double [] computeTimeIncrements(Trajectory traj) {
        int n = traj.getPoints();
        double [] dt = new double[n];
        dt[n-1] = 0;
        for (int i=0;i<n-1;i++)
            dt[i] = traj.getData(0, i+1) - traj.getData(0, i);
        return dt;
    }
    
    private double [] computeTimeIncrements(double[][] traj) {
        int n = traj[0].length;
        double [] dt = new double[n];
        dt[n-1] = 0;
        for (int i=0;i<n-1;i++)
            dt[i] = traj[0][i+1] - traj[0][i];
        return dt;
    }
    
    
    

    void setAtomicTruthValuePointwiseSemantics(Trajectory traj) {
        double[] v = new double[this.storeVars];
        for (int i=0;i<traj.getPoints();i++) {
            //store variables are stored in position 1 to storeVars
            for (int j=1;j<=storeVars;j++)
                v[j-1] = traj.getData(j, i);
            store.setAllVariableValues(v);
            for (MTLatom n : this.atomicNodes)
                n.computeTruthValue(i);
        }
    }
    
    void setAtomicTruthValuePointwiseSemantics(double[][] traj) {
        double[] v = new double[this.storeVars];
        for (int i=0;i<traj[0].length;i++) {
            //store variables are stored in position 1 to storeVars
            for (int j=1;j<=storeVars;j++)
                v[j-1] = traj[j][i];
            store.setAllVariableValues(v);
            for (MTLatom n : this.atomicNodes)
                n.computeTruthValue(i);
        }
    }
    
    
    private int getRealLength(Trajectory traj) {
        for (int i=1;i<traj.getPoints();i++) {
            if (traj.getData(0, i)<=0)
                return i;
        }
        return traj.getPoints();
    }
    
    private int getRealLength(double[][] traj) {
        for (int i=1;i<traj[0].length;i++) {
            if (traj[0][i]<=0)
                return i;
        }
        return traj[0].length;
    }
    
    
    void setAtomicTruthValueBooleanSemantics(Trajectory traj) {
        int m = getRealLength(traj);
        double[] v = new double[this.storeVars];
        double[] t = new double[m];
        double[][] X = new double[atomicNodes.size()][m];
        for (int i=0;i<m;i++) {
            //store variables are stored in position 1 to storeVars
            t[i] = traj.getData(0, i);
            for (int j=1;j<=storeVars;j++)
                v[j-1] = traj.getData(j, i);
            store.setAllVariableValues(v);
            for (int k=0;k<atomicNodes.size();k++) {
                MTLatom n = this.atomicNodes.get(k);
                X[k][i] = n.computePredicateValue(); 
            }  
        }
        for (int k=0;k<atomicNodes.size();k++) {
            MTLatom n = this.atomicNodes.get(k);
            BooleanSignal s;
            if (this.piecewiseLinearSignals)  
                s = BooleanSignalTransducer.convertPWLinearSignalToBoolean(t, X[k], !n.isStrictInequality());
            else
                s = BooleanSignalTransducer.convertPWConstantSignalToBoolean(t, X[k], !n.isStrictInequality());
            n.signal = s;
        }    
    }

    
    void setAtomicTruthValueBooleanSemantics(double[][] traj) {
        int m = getRealLength(traj);
        double[] v = new double[this.storeVars];
        double[] t = new double[m];
        double[][] X = new double[atomicNodes.size()][m];
        for (int i=0;i<m;i++) {
            //store variables are stored in position 1 to storeVars
            for (int j=1;j<=storeVars;j++)
                v[j-1] = traj[j][i];
            store.setAllVariableValues(v);
            for (int k=0;k<atomicNodes.size();k++) {
                MTLatom n = this.atomicNodes.get(k);
                X[k][i] = n.computePredicateValue(); 
            }  
        }
        for (int k=0;k<atomicNodes.size();k++) {
            MTLatom n = this.atomicNodes.get(k);
            BooleanSignal s;
            if (this.piecewiseLinearSignals)  
                s = BooleanSignalTransducer.convertPWLinearSignalToBoolean(t, X[k], !n.isStrictInequality());
            else
                s = BooleanSignalTransducer.convertPWConstantSignalToBoolean(t, X[k], !n.isStrictInequality());
            n.signal = s;
        }
    }
    
    public boolean performsEstimate() {
        return this.estimate;
    }
    
    /**
     * Signals the abnormal termination of the model checking procedure. 
     */
    public void terminateModelChecking() {
        controller.finalization();
        this.ready = false;
    }
    
    
    public SMCoutput getSMCoutput() {
        if (ready)
            throw new RuntimeException("The model checking is not completed");
        if (estimate)
            if (this.estimator == null)
                throw new RuntimeException("Output is not present, I'm puzzled, sorry!");
            else return estimator;
        else
            if (this.tester == null)
                throw new RuntimeException("Output is not present, I'm puzzled, sorry!");
            else return tester;
    }
    
    
    
    
    public MTLformula duplicate() {
        MTLformula f = new MTLformula(root.duplicate());
        return f;
    }
    
    
    public ArrayList<MTLnode> getAllNodes() {
        ArrayList<MTLnode> list = new ArrayList<MTLnode>();
        root.collectSubtreeNodes(list);
        return list;
    }
    
    public ArrayList<String> getAtomicExpressions() {
        ArrayList<String> list = new ArrayList<String>();
        for (MTLnode n : this.atomicNodes) {
            if (n instanceof MTLatom) {
                MTLatom a = (MTLatom)n;
                list.add(a.predicateString);
            }
        }
        return list;
    }
    
    
    public MTLnode randomNode() {
        root.countNodes();
        MTLnode current = root;
        int leftSubtree, rightSubtree, childSubtree;
                
        while (true) {
            childSubtree = (current.child != null ? current.child.subtreeSize : 0); 
            leftSubtree =  (current.left  != null ? current.left.subtreeSize  : 0);
            rightSubtree = (current.right != null ? current.right.subtreeSize : 0);
            double[] x = {1,childSubtree,leftSubtree,rightSubtree};
            int k = RandomGenerator.sample(x);
            switch(k) {
                case 0:
                    return current;
                case 1: 
                    current = current.child;
                    break;
                case 2:
                    current = current.left;
                    break;
                case 3: 
                    current = current.right;
                    break;
            }
        }
    }
    
    public MTLnode randomInternalNode() {
        int h = root.countInternalNodes();
        MTLnode current = root;
        int leftSubtree, rightSubtree, childSubtree;
        if (h == 0)
            return null;
                
        while (true) {
            childSubtree = (current.child != null ? current.child.internalSubtreeSize : 0); 
            leftSubtree =  (current.left  != null ? current.left.internalSubtreeSize  : 0);
            rightSubtree = (current.right != null ? current.right.internalSubtreeSize : 0);
            double[] x = {1,childSubtree,leftSubtree,rightSubtree};
            int k = RandomGenerator.sample(x);
            switch(k) {
                case 0:
                    return current;
                case 1: 
                    current = current.child;
                    break;
                case 2:
                    current = current.left;
                    break;
                case 3: 
                    current = current.right;
                    break;
            }
        }
    }
    
    public void setNewRoot(MTLnode newroot) {
        this.root = newroot;
    }
    
    
    public int countNodes() {
        return root.countNodes();
    }
    
    public int countLeaves() {
        return root.countLeaves();
    }
    
    public int countTemporalNodes() {
        return root.countTemporalNodes();
    }
    
    public int countBooleanNodes() {
        return root.countBooleanNodes();
    }
    
    public int countInternalNodes() {
        return root.countInternalNodes();
    }

    
    public ArrayList<String> getParameters() {
        ArrayList<String> list = new ArrayList<String>();
        return root.getParameters(list);
    }    
    
    
   
    
    /*
     * Notacce: devo gestire il setup del metodo e il setuo della struttura dati di output
     * devo mettere in piedi agoritmo, in cui faccio un while la struttura dati di output 
     * non mi dice stop o non raggiugno un upper bound?
     * La strttura dati di putput mi viene passata da fuori.
     *
     *
     *
     */

}

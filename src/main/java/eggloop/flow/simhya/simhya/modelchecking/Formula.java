/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.dataprocessing.Trajectory;
import java.util.HashMap;

/**
 *
 * @author Luca
 */
public interface Formula {
    
    /**
     * Initialises the formula, parsing atomic predicates and linking them to a model
     * @param store the store containing infor  about model variables
     * @param localSymbols local parameters of the formula. 
     */
    public void initalize(Store store, SymbolArray localSymbols, HashMap predicatesToExpression);
    /**
     * recomputes bounds of modal operators.
     * @param localSymbols 
     */
    public void recomputeBounds(SymbolArray localSymbols);
    
    
    

   
    /**
     * Initializes the formula for model checking, if the formula requires estimation.
     * @param estimator an estimator object, which will be storing info about estimated probabilities
     * @param parameter a parameter for choosing the varying interval, can be null, and in this case it is ignored
     * @param varyInterval true if the extimation has to sweep the top metric interval
     * @param varyRightBound true if the sweeping starts from a point and expands to the right to the full interval, 
     * false if it starts with the full interval and contracts it to a point on the left.
     * @param points the number of points to sampe from the interval.
     */
    public void initializeForEstimate(SMCestimateTrajectory estimator, String parameter,
                                      boolean varyInterval, boolean varyRightBound, int points);
    
    
    /**
     *  Initializes the formula for model checking, if the formula tests if the probability
     * is below or above a ginve threshold.
     * @param tester 
     */
    public void initializeForModelChecking(SMCtest tester);
    
    
    /**
     * this is the key method to call in a routine to model check a formula.
     * A simulator has to be used to feed trajectories to the method, one by one. 
     * Then each trajectory is model checked and the proper action is performed in 
     * terms of estimate or testing.
     * The method returns true if and ony if the model checking procedure has reached
     * an end, according to current accuracy criteria.
     * @param traj
     * @return 
     */
    public boolean modelCheckNextTrajectoryPointwiseSemantics(Trajectory traj);
    
}
